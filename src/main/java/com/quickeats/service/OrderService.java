package com.quickeats.service;

import com.quickeats.dto.CreateOrderDTO;
import com.quickeats.dto.NewOrderEventDTO;
import com.quickeats.dto.OrderItemDTO;
import com.quickeats.dto.OrderResponseDTO;
import com.quickeats.dto.OrderStatusUpdateDTO;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.Order;
import com.quickeats.model.OrderStatus;
import com.quickeats.model.Restaurant;
import com.quickeats.model.User;
import com.quickeats.model.Menu;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    public OrderResponseDTO placeOrder(CreateOrderDTO createOrderDTO) {
        User user = userService.getUserById(createOrderDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + createOrderDTO.getUserId()));

        Restaurant restaurant = restaurantService.getRestaurantEntityById(createOrderDTO.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + createOrderDTO.getRestaurantId()));

        double computedTotal = 0.0;
        if (createOrderDTO.getItems() != null) {
            for (OrderItemDTO item : createOrderDTO.getItems()) {
                if (item.getMenuId() == null) {
                    throw new ResourceNotFoundException("Menu ID is required for order items");
                }
                Menu menu = menuRepository.findById(item.getMenuId())
                        .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + item.getMenuId()));

                if (menu.getRestaurant() == null || !menu.getRestaurant().getId().equals(restaurant.getId())) {
                    throw new ResourceNotFoundException("Menu item with id: " + item.getMenuId() + " does not belong to restaurant id: " + restaurant.getId());
                }

                item.setPrice(menu.getPrice());
                if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
                    item.setItemName(menu.getItemName());
                }
                computedTotal += menu.getPrice() * item.getQuantity();
            }
        }
        Double totalAmount = computedTotal;

        try {
            String orderItemsJson = objectMapper.writeValueAsString(createOrderDTO.getItems());

            Order order = new Order(user, restaurant, totalAmount, orderItemsJson);
            order.setStatus("PENDING");

            Order savedOrder = orderRepository.save(order);
            OrderResponseDTO dto = convertToOrderResponseDTO(savedOrder, createOrderDTO.getItems());

            broadcastStatusUpdate(savedOrder, 25, 28.6289, 77.2185);
            broadcastAdminOrderEvent(savedOrder, createOrderDTO.getItems().size());

            return dto;

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error processing order items JSON", e);
        }
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::convertToOrderResponseDTO);
    }

    public Page<OrderResponseDTO> getOrdersByUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(this::convertToOrderResponseDTO);
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Page<OrderResponseDTO> getOrdersByRestaurant(Long restaurantId, Pageable pageable) {
        return orderRepository.findByRestaurantId(restaurantId, pageable).map(this::convertToOrderResponseDTO);
    }

    public List<Order> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    @Autowired(required = false)
    private FcmService fcmService;

    public OrderResponseDTO updateOrderStatus(Long orderId, String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status, e);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setStatus(orderStatus.name());
        Order updated = orderRepository.save(order);

        int eta = "DELIVERED".equalsIgnoreCase(status) ? 0 : ("OUT_FOR_DELIVERY".equalsIgnoreCase(status) ? 12 : 20);
        broadcastStatusUpdate(updated, eta, 28.6289, 77.2185);
        broadcastAdminOrderEvent(updated, 1);

        // Trigger FCM Push Notification gracefully
        if (fcmService != null && updated.getUser() != null && updated.getUser().getFcmDeviceToken() != null) {
            try {
                String pushTitle = "Order #" + updated.getId() + " Update!";
                String pushBody = "DELIVERED".equalsIgnoreCase(status) 
                    ? "Your order has been Delivered! Enjoy your meal! 🍔"
                    : ("OUT_FOR_DELIVERY".equalsIgnoreCase(status)
                        ? "Your order is now Out for Delivery! 🛵"
                        : "Your order status is now " + status + ".");
                fcmService.sendPushNotification(updated.getUser().getFcmDeviceToken(), pushTitle, pushBody);
            } catch (Exception e) {
                logger.warn("FCM push notification attempt failed gracefully: {}", e.getMessage());
            }
        }

        return convertToOrderResponseDTO(updated);
    }

    public OrderResponseDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        String currentStatus = order.getStatus() != null ? order.getStatus().toUpperCase() : "";
        if (!"PENDING".equals(currentStatus) && !"PREPARING".equals(currentStatus)) {
            throw new IllegalStateException("Only PENDING or PREPARING orders can be cancelled.");
        }

        order.setStatus("CANCELLED");
        Order updated = orderRepository.save(order);

        broadcastStatusUpdate(updated, 0, 28.6289, 77.2185);
        broadcastAdminOrderEvent(updated, 1);

        if (fcmService != null && updated.getUser() != null && updated.getUser().getFcmDeviceToken() != null) {
            try {
                fcmService.sendPushNotification(
                    updated.getUser().getFcmDeviceToken(),
                    "Order #" + orderId + " Cancelled",
                    "Your order #" + orderId + " has been cancelled successfully."
                );
            } catch (Exception e) {
                logger.warn("FCM push notification attempt failed: {}", e.getMessage());
            }
        }

        return convertToOrderResponseDTO(updated);
    }

    public void broadcastStatusUpdate(Order order, int etaMinutes, double lat, double lng) {
        if (messagingTemplate != null) {
            try {
                OrderStatus orderStatus;
                try {
                    orderStatus = OrderStatus.valueOf(order.getStatus().toUpperCase());
                } catch (Exception e) {
                    orderStatus = OrderStatus.PENDING;
                }

                OrderStatusUpdateDTO dto = new OrderStatusUpdateDTO(
                    order.getId(),
                    orderStatus,
                    "Ramesh Kumar",
                    "+91 98765 43210",
                    etaMinutes,
                    lat,
                    lng
                );
                messagingTemplate.convertAndSend("/topic/orders/" + order.getId(), dto);
                logger.info("Broadcasted WebSocket update for order #{}", order.getId());
            } catch (Exception e) {
                logger.warn("Failed to broadcast WebSocket update for order #{}: {}", order.getId(), e.getMessage());
            }
        }
    }

    public void broadcastAdminOrderEvent(Order order, int itemCount) {
        if (messagingTemplate != null) {
            try {
                NewOrderEventDTO adminDto = new NewOrderEventDTO(
                    order.getId(),
                    order.getUser() != null ? order.getUser().getName() : "Customer",
                    order.getRestaurant() != null ? order.getRestaurant().getName() : "Restaurant",
                    itemCount,
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getOrderTime() != null ? order.getOrderTime() : LocalDateTime.now()
                );
                messagingTemplate.convertAndSend("/topic/admin/orders", adminDto);
                logger.info("Broadcasted Admin WebSocket notification for order #{}", order.getId());
            } catch (Exception e) {
                logger.warn("Failed to broadcast Admin order event for order #{}: {}", order.getId(), e.getMessage());
            }
        }
    }

    public Page<OrderResponseDTO> getOrdersByUserAndStatus(Long userId, String status, Pageable pageable) {
        return orderRepository.findByUserIdAndStatus(userId, status, pageable).map(this::convertToOrderResponseDTO);
    }

    public Page<OrderResponseDTO> getOrdersByRestaurantAndStatus(Long restaurantId, String status, Pageable pageable) {
        return orderRepository.findByRestaurantIdAndStatus(restaurantId, status, pageable).map(this::convertToOrderResponseDTO);
    }

    public Page<OrderResponseDTO> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findOrdersBetweenDates(startDate, endDate, pageable).map(this::convertToOrderResponseDTO);
    }

    public Long getOrdersCountByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    public OrderResponseDTO convertToOrderResponseDTO(Order order) {
        if (order == null) {
            return null;
        }
        List<OrderItemDTO> items = Collections.emptyList();
        if (order.getOrderItems() != null && !order.getOrderItems().trim().isEmpty()) {
            try {
                items = objectMapper.readValue(
                    order.getOrderItems(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, OrderItemDTO.class)
                );
            } catch (JsonProcessingException e) {
                // fallback to empty list
            }
        }
        return convertToOrderResponseDTO(order, items);
    }

    private OrderResponseDTO convertToOrderResponseDTO(Order order, List<OrderItemDTO> items) {
        return new OrderResponseDTO(
            order.getId(),
            order.getUser().getId(),
            order.getUser().getName(),
            order.getRestaurant().getId(),
            order.getRestaurant().getName(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getOrderTime(),
            items
        );
    }

    public OrderResponseDTO getOrderResponseDTO(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        return convertToOrderResponseDTO(order);
    }
}
