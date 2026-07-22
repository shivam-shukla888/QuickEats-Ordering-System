package com.quickeats.orchestrator;

import com.quickeats.agent.OrderingAgentService;
import com.quickeats.rag.RecommendationService;
import com.quickeats.support.SupportAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AgentOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(AgentOrchestrator.class);

    @Autowired
    private OrderingAgentService orderingAgentService;

    @Autowired
    private SupportAgentService supportAgentService;

    @Autowired
    private RecommendationService recommendationService;

    public OrchestrationResult routeAndExecute(Long userId, String userMessage) {
        if (userId == null) userId = 1L;
        if (userMessage == null) userMessage = "";

        String intent = classifyIntent(userMessage);
        logger.info("AgentOrchestrator classified intent as '{}' for user message: '{}'", intent, userMessage);

        if ("COMPLAINT_SUPPORT".equals(intent)) {
            Long orderId = extractOrderId(userMessage);
            SupportAgentService.ComplaintResult complaintResult = supportAgentService.handleComplaint(userId, orderId, userMessage);
            return new OrchestrationResult("SupportAgentService", "COMPLAINT_SUPPORT", complaintResult);
        } else if ("FOOD_RECOMMENDATION".equals(intent)) {
            RecommendationService.RecommendationResult ragResult = recommendationService.getRecommendations(userId, userMessage);
            return new OrchestrationResult("RecommendationService (RAG)", "FOOD_RECOMMENDATION", ragResult);
        } else {
            OrderingAgentService.AgentResponse agentResponse = orderingAgentService.processMessage(userId, userMessage);
            return new OrchestrationResult("OrderingAgentService", "ORDERING_SEARCH", agentResponse);
        }
    }

    private String classifyIntent(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("refund") || lower.contains("wrong item") || lower.contains("complain") ||
            lower.contains("bad quality") || lower.contains("spoiled") || lower.contains("cold food") ||
            lower.contains("late delivery") || lower.contains("delayed") || lower.contains("where is my refund") ||
            lower.contains("missing item") || lower.contains("issue with order")) {
            return "COMPLAINT_SUPPORT";
        }

        if (lower.contains("recommend") || lower.contains("craving") || lower.contains("something light") ||
            lower.contains("comfort food") || lower.contains("suggest") || lower.contains("healthy dinner") ||
            lower.contains("feeling low")) {
            return "FOOD_RECOMMENDATION";
        }

        return "ORDERING_SEARCH";
    }

    private Long extractOrderId(String text) {
        Matcher m = Pattern.compile("(?:order|#)\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(text);
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        return 1L;
    }

    public static class OrchestrationResult {
        private String agentHandled;
        private String intent;
        private Object response;

        public OrchestrationResult(String agentHandled, String intent, Object response) {
            this.agentHandled = agentHandled;
            this.intent = intent;
            this.response = response;
        }

        public String getAgentHandled() { return agentHandled; }
        public String getIntent() { return intent; }
        public Object getResponse() { return response; }
    }
}
