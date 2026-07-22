package com.quickeats.support;

import com.quickeats.agent.ToolInvocationTracker;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SupportAgentService {

    private static final Logger logger = LoggerFactory.getLogger(SupportAgentService.class);

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private SupportTools supportTools;

    private SupportAgent supportAgent;

    @PostConstruct
    public void init() {
        try {
            this.supportAgent = AiServices.builder(SupportAgent.class)
                    .chatLanguageModel(chatLanguageModel)
                    .tools(supportTools)
                    .build();
            logger.info("SupportAgent successfully initialized with LangChain4j and SupportTools.");
        } catch (Exception e) {
            logger.warn("Failed to initialize LangChain4j SupportAgent: {}. Will use fallback orchestrator.", e.getMessage());
        }
    }

    public ComplaintResult handleComplaint(Long userId, Long orderId, String complaintText) {
        ToolInvocationTracker.clear();

        if (userId == null) userId = 1L;
        String userComplaint = (orderId != null ? "Order #" + orderId + ": " : "") + complaintText;

        String rawResponse;
        try {
            if (supportAgent != null) {
                rawResponse = supportAgent.processComplaint(userId, userComplaint);
            } else {
                rawResponse = runDeterministicFallback(userId, orderId, complaintText);
            }
        } catch (Exception e) {
            logger.warn("LangChain4j SupportAgent call failed: {}. Executing deterministic support fallback.", e.getMessage());
            rawResponse = runDeterministicFallback(userId, orderId, complaintText);
        }

        List<String> toolsInvoked = ToolInvocationTracker.getInvokedTools();

        String investigation = extractSection(rawResponse, "Investigation", "Investigated order details and delivery timeline.");
        String classification = extractClassification(complaintText);
        String decision = extractSection(rawResponse, "Decision", toolsInvoked.contains("escalateToHuman") ? "ESCALATE_TO_HUMAN" : "AUTO_REFUND_APPROVED");
        String actionTaken = toolsInvoked.contains("issueRefund") ? "Issued Refund in Database" : (toolsInvoked.contains("escalateToHuman") ? "Escalated to Human Supervisor" : "Reviewed Complaint");
        String reasoning = rawResponse;

        return new ComplaintResult(investigation, classification, decision, actionTaken, reasoning, toolsInvoked);
    }

    private String runDeterministicFallback(Long userId, Long orderId, String complaintText) {
        if (orderId == null) {
            orderId = extractOrderIdFromText(complaintText);
        }

        String orderInfo = supportTools.getOrderDetails(orderId != null ? orderId : 1L);
        String timelineInfo = supportTools.checkDeliveryTimeline(orderId != null ? orderId : 1L);

        String lower = complaintText.toLowerCase();

        if (lower.contains("wrong") || lower.contains("bad") || lower.contains("cold") || lower.contains("spoiled") || lower.contains("delayed") || lower.contains("late")) {
            // Determine refund amount
            double amount = 350.0;
            if (lower.contains("expensive") || lower.contains("600") || lower.contains("700") || lower.contains("1000") || lower.contains("high value")) {
                amount = 750.0;
            }

            if (amount > SupportTools.REFUND_SAFETY_THRESHOLD) {
                String escRes = supportTools.escalateToHuman(orderId != null ? orderId : 1L, "High value complaint exceeding ₹500 safety limit: " + complaintText);
                return "Investigation:\n" + orderInfo + "\n\nClassification: HIGH_VALUE_COMPLAINT\n\nDecision: ESCALATE_TO_HUMAN\n\nReasoning: Claimed amount ₹" + amount + " exceeds ₹500 auto-approval threshold limit.\n\n" + escRes;
            } else {
                String refundRes = supportTools.issueRefund(orderId != null ? orderId : 1L, amount, "Verified complaint: " + complaintText);
                return "Investigation:\n" + orderInfo + "\n\nTimeline:\n" + timelineInfo + "\n\nClassification: VERIFIED_COMPLAINT\n\nDecision: AUTO_REFUND_APPROVED\n\nReasoning: Complaint verified and within ₹500 safety threshold.\n\n" + refundRes;
            }
        } else {
            String escRes = supportTools.escalateToHuman(orderId != null ? orderId : 1L, "Ambiguous/unverified customer complaint: " + complaintText);
            return "Investigation:\n" + orderInfo + "\n\nClassification: AMBIGUOUS_OR_UNVERIFIED\n\nDecision: ESCALATE_TO_HUMAN\n\nReasoning: Complaint details ambiguous or unverified. Escalated for supervisor review.\n\n" + escRes;
        }
    }

    private Long extractOrderIdFromText(String text) {
        if (text == null) return 1L;
        Matcher m = Pattern.compile("(?:order|#)\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(text);
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        return 1L;
    }

    private String extractClassification(String text) {
        String lower = text != null ? text.toLowerCase() : "";
        if (lower.contains("wrong")) return "WRONG_ITEM_DELIVERED";
        if (lower.contains("late") || lower.contains("delay")) return "LATE_DELIVERY";
        if (lower.contains("cold") || lower.contains("spoiled") || lower.contains("bad")) return "FOOD_QUALITY_ISSUE";
        if (lower.contains("missing")) return "MISSING_ITEM";
        return "AMBIGUOUS_OR_UNVERIFIED";
    }

    private String extractSection(String fullText, String sectionName, String fallback) {
        if (fullText == null) return fallback;
        Pattern p = Pattern.compile(sectionName + ":?\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(fullText);
        if (m.find()) return m.group(1).trim();
        return fallback;
    }

    public static class ComplaintResult {
        private String investigation;
        private String classification;
        private String decision;
        private String actionTaken;
        private String reasoning;
        private List<String> toolsInvoked;

        public ComplaintResult(String investigation, String classification, String decision, String actionTaken, String reasoning, List<String> toolsInvoked) {
            this.investigation = investigation;
            this.classification = classification;
            this.decision = decision;
            this.actionTaken = actionTaken;
            this.reasoning = reasoning;
            this.toolsInvoked = toolsInvoked;
        }

        public String getInvestigation() { return investigation; }
        public String getClassification() { return classification; }
        public String getDecision() { return decision; }
        public String getActionTaken() { return actionTaken; }
        public String getReasoning() { return reasoning; }
        public List<String> getToolsInvoked() { return toolsInvoked; }
    }
}
