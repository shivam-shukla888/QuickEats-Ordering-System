package com.quickeats.support;

import com.quickeats.agent.ToolInvocationTracker;
import com.quickeats.model.Order;
import com.quickeats.repository.OrderRepository;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class SupportTools {

    private static final Logger logger = LoggerFactory.getLogger(SupportTools.class);
    public static final double REFUND_SAFETY_THRESHOLD = 500.0;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private EscalationRepository escalationRepository;

    @Tool("Fetch order details, customer info, order items, total amount, and creation timestamp by order ID.")
    public String getOrderDetails(Long orderId) {
        ToolInvocationTracker.logToolCall("getOrderDetails");

        if (orderId == null) {
            return "Error: Order ID is required.";
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return "Error: Order #" + orderId + " not found in system database.";
        }

        Order order = orderOpt.get();
        String customerName = order.getUser() != null ? order.getUser().getName() : "Customer";
        String restName = order.getRestaurant() != null ? order.getRestaurant().getName() : "North Indian Kitchen";

        return String.format("Order Details for #%d:\n• Customer: %s\n• Restaurant: %s\n• Total Amount: ₹%.2f\n• Status: %s\n• Created At: %s\n• Items JSON: %s",
                order.getId(),
                customerName,
                restName,
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderTime() != null ? order.getOrderTime().toString() : "Recent",
                order.getOrderItems() != null ? order.getOrderItems() : "[]"
        );
    }

    @Tool("Check delivery timeline for an order to verify if it was delayed beyond expected delivery window.")
    public String checkDeliveryTimeline(Long orderId) {
        ToolInvocationTracker.logToolCall("checkDeliveryTimeline");

        if (orderId == null) return "Error: Order ID is required.";

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) return "Order #" + orderId + " not found.";

        Order order = orderOpt.get();
        LocalDateTime orderTime = order.getOrderTime() != null ? order.getOrderTime() : LocalDateTime.now().minusMinutes(45);
        long elapsedMinutes = Duration.between(orderTime, LocalDateTime.now()).toMinutes();

        boolean isDelayed = elapsedMinutes > 30 && !"DELIVERED".equalsIgnoreCase(order.getStatus());

        return String.format("Delivery Timeline Analysis for Order #%d:\n• Elapsed Time: %d minutes\n• Current Status: %s\n• Is Delayed: %s (Standard ETA: 30 mins)",
                orderId, elapsedMinutes, order.getStatus(), isDelayed ? "YES (DELAY CONFIRMED)" : "NO (ON TIME)"
        );
    }

    @Tool("Issue a monetary refund for an order up to the maximum auto-approval limit of ₹500.")
    public String issueRefund(Long orderId, Double amount, String reason) {
        ToolInvocationTracker.logToolCall("issueRefund");

        if (orderId == null || amount == null || amount <= 0) {
            return "Error: Valid order ID and positive refund amount are required.";
        }

        // HARD-CODED JAVA SAFETY GUARDRAIL: Strict maximum auto-approval limit of ₹500
        if (amount > REFUND_SAFETY_THRESHOLD) {
            logger.warn("SAFETY GUARDRAIL ENFORCED: Refund amount ₹{} exceeds max threshold ₹{}. Escalating to human supervisor.", amount, REFUND_SAFETY_THRESHOLD);
            return escalateToHuman(orderId, "Requested refund amount ₹" + amount + " exceeds max auto-approval safety threshold of ₹" + REFUND_SAFETY_THRESHOLD + ". Reason: " + reason);
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        Long userId = orderOpt.map(o -> o.getUser() != null ? o.getUser().getId() : 1L).orElse(1L);

        Refund refund = new Refund(orderId, userId, amount, reason, "APPROVED");
        refundRepository.save(refund);

        orderOpt.ifPresent(o -> {
            o.setStatus("CANCELLED");
            orderRepository.save(o);
        });

        return String.format("✅ REFUND APPROVED & PROCESSED:\n• Refund ID: #%d\n• Order ID: #%d\n• Amount Refunded: ₹%.2f\n• Reason: %s\n• Status: APPROVED\n• Order Status Updated to: CANCELLED",
                refund.getId(), orderId, amount, reason
        );
    }

    @Tool("Escalate an unresolved or high-value customer complaint to a human supervisor.")
    public String escalateToHuman(Long orderId, String reason) {
        ToolInvocationTracker.logToolCall("escalateToHuman");

        if (orderId == null) orderId = 0L;

        Escalation escalation = new Escalation(orderId, reason, "PENDING_SUPERVISOR_REVIEW");
        escalationRepository.save(escalation);

        return String.format("⚠️ COMPLAINT ESCALATED TO HUMAN SUPERVISOR:\n• Escalation ID: #%d\n• Order ID: #%d\n• Escalation Reason: %s\n• Priority: HIGH\n• Status: PENDING_SUPERVISOR_REVIEW (Senior Operations Lead notified)",
                escalation.getId(), orderId, reason
        );
    }
}
