package com.quickeats.service;

import com.quickeats.model.Order;
import com.quickeats.model.User;
import com.quickeats.repository.OrderRepository;
import com.quickeats.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ChatSupportService {

    private static final Logger logger = LoggerFactory.getLogger(ChatSupportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private GroqClientService groqClientService;

    public String handleSupportChat(Long userId, String userMessage) {
        logger.info("Processing AI Order Support request for userId={}", userId);

        String userName = "Valued Customer";
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                userName = userOpt.get().getName();
            }
        }

        // Fetch user's recent orders (last 5)
        StringBuilder orderContext = new StringBuilder();
        if (userId != null) {
            Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "orderTime"));
            List<Order> recentOrders = orderRepository.findByUserId(userId, pageable).getContent();

            if (recentOrders.isEmpty()) {
                orderContext.append("User has no past orders recorded.");
            } else {
                for (Order order : recentOrders) {
                    String restaurantName = (order.getRestaurant() != null) ? order.getRestaurant().getName() : "Unknown Restaurant";
                    String formattedDate = (order.getOrderTime() != null) ? order.getOrderTime().format(DATE_FORMATTER) : "N/A";

                    orderContext.append(String.format("- Order #%d: Restaurant: %s | Status: %s | Amount: $%.2f | Date: %s | Items: %s\n",
                            order.getId(),
                            restaurantName,
                            order.getStatus(),
                            order.getTotalAmount() != null ? order.getTotalAmount() : 0.0,
                            formattedDate,
                            order.getOrderItems() != null ? order.getOrderItems() : "N/A"
                    ));
                }
            }
        } else {
            orderContext.append("User ID not specified. No order history available.");
        }

        String systemPrompt = String.format("""
                You are QuickEats Customer Support AI, a friendly and helpful virtual support assistant for the QuickEats food ordering platform.
                
                Customer Name: %s
                Recent Order History (last 5 orders):
                %s
                
                Guidelines:
                1. Answer questions about the user's order statuses, order details, order history, or general food ordering topics.
                2. Be direct, accurate, friendly, and concise.
                3. If asked "where is my order" or about order status, refer to the most recent relevant order from the history provided above.
                4. If asked about previous purchases or order history, summarize the relevant orders.
                5. Maintain a polite and professional tone at all times.
                """, userName, orderContext.toString());

        try {
            String response = groqClientService.chatCompletion(systemPrompt, userMessage);
            if (response != null && !response.trim().isEmpty()) {
                return response.trim();
            }
        } catch (Exception e) {
            logger.warn("Groq API call failed for support chat (userId={}): {}. Returning graceful fallback.", userId, e.getMessage());
        }

        return buildFallbackResponse(userId);
    }

    private String buildFallbackResponse(Long userId) {
        if (userId != null) {
            List<Order> recentOrders = orderRepository.findByUserId(userId);
            if (!recentOrders.isEmpty()) {
                Order latest = recentOrders.get(recentOrders.size() - 1);
                String restName = (latest.getRestaurant() != null) ? latest.getRestaurant().getName() : "QuickEats Restaurant";
                return String.format("I'm having trouble reaching our AI support right now. However, your latest order #%d from %s is currently marked as '%s'. You can track all order details on your Order History page!",
                        latest.getId(), restName, latest.getStatus());
            }
        }
        return "I'm currently having trouble connecting to AI support. Please check your Order History page or try again in a few moments.";
    }
}
