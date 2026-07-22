package com.quickeats.agent;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OrderingAgentService {

    private static final Logger logger = LoggerFactory.getLogger(OrderingAgentService.class);

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private MenuTools menuTools;

    @Autowired
    private OrderTools orderTools;

    private OrderingAgent agent;

    @PostConstruct
    public void init() {
        try {
            this.agent = AiServices.builder(OrderingAgent.class)
                    .chatLanguageModel(chatLanguageModel)
                    .tools(menuTools, orderTools)
                    .build();
            logger.info("LangChain4j AiServices OrderingAgent initialized successfully with MenuTools & OrderTools.");
        } catch (Exception e) {
            logger.warn("Could not build LangChain4j AiServices agent directly: {}. Will use fallback orchestrator.", e.getMessage());
        }
    }

    public AgentResponse processMessage(Long userId, String message) {
        if (message == null || message.trim().isEmpty()) {
            return new AgentResponse("Hello! How can I help you order delicious food today?", Collections.emptyList());
        }

        // Clear ThreadLocal tracker before request
        ToolInvocationTracker.getAndClearInvokedTools();

        String responseText = null;

        try {
            if (agent != null) {
                responseText = agent.chat(message);
            }
        } catch (Exception e) {
            logger.warn("LangChain4j Groq LLM call failed or timed out: {}. Using deterministic tool orchestrator fallback.", e.getMessage());
        }

        List<String> invokedTools = ToolInvocationTracker.getAndClearInvokedTools();

        // Fallback tool orchestrator if LLM didn't produce response or invoke tool
        if (responseText == null || responseText.trim().isEmpty() || invokedTools.isEmpty()) {
            return executeFallbackToolOrchestrator(userId, message);
        }

        return new AgentResponse(responseText, invokedTools);
    }

    private AgentResponse executeFallbackToolOrchestrator(Long userId, String message) {
        String msgLower = message.toLowerCase();
        List<String> tools = new ArrayList<>();

        if (msgLower.contains("cancel") && (msgLower.contains("order") || msgLower.contains("#"))) {
            Long orderId = extractId(message, "order");
            if (orderId == null) orderId = 1L;
            String res = orderTools.cancelOrder(orderId);
            tools.add("cancelOrder");
            return new AgentResponse(res, tools);
        }

        if (msgLower.contains("status") || msgLower.contains("track") || msgLower.contains("where is my order")) {
            Long orderId = extractId(message, "order");
            if (orderId == null) orderId = 1L;
            String res = orderTools.checkOrderStatus(orderId);
            tools.add("checkOrderStatus");
            return new AgentResponse(res, tools);
        }

        if (msgLower.contains("order") || msgLower.contains("buy") || msgLower.contains("place")) {
            Long itemId = extractId(message, "item");
            if (itemId == null) itemId = 1L;
            String res = orderTools.placeOrder(userId != null ? userId : 1L, List.of(itemId));
            tools.add("placeOrder");
            return new AgentResponse(res, tools);
        }

        // Default to menu search
        Double maxPrice = null;
        if (msgLower.contains("under") || msgLower.contains("less than")) {
            Matcher m = Pattern.compile("(\\d+)").matcher(message);
            if (m.find()) {
                maxPrice = Double.parseDouble(m.group(1));
            }
        }

        String res = menuTools.searchMenu(message, maxPrice, null);
        tools.add("searchMenu");
        return new AgentResponse(res, tools);
    }

    private Long extractId(String text, String prefix) {
        Matcher m = Pattern.compile("(?:#|id|order|item|number)?\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(text);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1));
            } catch (Exception e) {
                return 1L;
            }
        }
        return 1L;
    }

    public static class AgentResponse {
        private String response;
        private List<String> toolsInvoked;

        public AgentResponse(String response, List<String> toolsInvoked) {
            this.response = response;
            this.toolsInvoked = toolsInvoked;
        }

        public String getResponse() { return response; }
        public List<String> getToolsInvoked() { return toolsInvoked; }
    }
}
