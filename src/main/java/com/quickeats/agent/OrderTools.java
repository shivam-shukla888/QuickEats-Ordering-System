package com.quickeats.agent;

import com.quickeats.dto.CreateOrderDTO;
import com.quickeats.dto.OrderItemDTO;
import com.quickeats.dto.OrderResponseDTO;
import com.quickeats.model.Menu;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.OrderRepository;
import com.quickeats.service.OrderService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderTools {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private com.quickeats.repository.UserRepository userRepository;

    @Tool("Place a new food order by providing user ID and list of menu item IDs.")
    public String placeOrder(Long userId, List<Long> menuItemIds) {
        ToolInvocationTracker.logToolCall("placeOrder");

        // Find existing user or create default customer user
        Long validUserId = userId;
        if (validUserId == null || !userRepository.existsById(validUserId)) {
            com.quickeats.model.User defaultUser = userRepository.findAll().stream().findFirst().orElseGet(() -> {
                com.quickeats.model.User newUser = new com.quickeats.model.User("Shivam Shukla", "shivam@quickeats.com", "pass123", "CUSTOMER");
                return userRepository.save(newUser);
            });
            validUserId = defaultUser.getId();
        }

        if (menuItemIds == null || menuItemIds.isEmpty()) {
            return "Cannot place order: Please specify at least one menu item ID.";
        }

        List<Menu> items = menuRepository.findAllById(menuItemIds);
        if (items.isEmpty()) {
            return "Cannot place order: None of the menu item IDs were found in our system.";
        }

        Long restaurantId = items.get(0).getRestaurant() != null ? items.get(0).getRestaurant().getId() : 1L;

        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setUserId(validUserId);
        createOrderDTO.setRestaurantId(restaurantId);

        List<OrderItemDTO> orderItems = items.stream().map(item ->
            new OrderItemDTO(item.getId(), 1, item.getPrice(), item.getItemName())
        ).collect(Collectors.toList());

        createOrderDTO.setItems(orderItems);

        try {
            OrderResponseDTO created = orderService.placeOrder(createOrderDTO);
            return String.format("🎉 Order Placed Successfully!\nOrder ID: #%d\nRestaurant: %s\nTotal Amount: ₹%.2f\nStatus: %s\nTrack live delivery at /orders/%d/track",
                    created.getId(),
                    created.getRestaurantName() != null ? created.getRestaurantName() : "North Indian Kitchen",
                    created.getTotalAmount(),
                    created.getStatus(),
                    created.getId()
            );
        } catch (Exception e) {
            return "Failed to place order: " + e.getMessage();
        }
    }

    @Tool("Cancel an active food order by providing the order ID.")
    public String cancelOrder(Long orderId) {
        ToolInvocationTracker.logToolCall("cancelOrder");

        if (orderId == null) {
            return "Cannot cancel order: Please provide a valid order ID.";
        }

        try {
            OrderResponseDTO cancelled = orderService.cancelOrder(orderId);
            return String.format("🚫 Order #%d has been cancelled successfully. Status: %s", cancelled.getId(), cancelled.getStatus());
        } catch (Exception e) {
            return "Failed to cancel order #" + orderId + ": " + e.getMessage();
        }
    }

    @Tool("Check the real-time status and details of a food order by providing the order ID.")
    public String checkOrderStatus(Long orderId) {
        ToolInvocationTracker.logToolCall("checkOrderStatus");

        if (orderId == null) {
            return "Please provide an order ID to check status.";
        }

        return orderRepository.findById(orderId)
                .map(order -> String.format("📦 Order #%d Status: %s | Total: ₹%.2f | Restaurant: %s | Time: %s",
                        order.getId(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getRestaurant() != null ? order.getRestaurant().getName() : "North Indian Dhaba",
                        order.getOrderTime() != null ? order.getOrderTime().toString() : "Recent"
                ))
                .orElse("Order #" + orderId + " not found in system.");
    }
}
