package com.quickeats.service;

import com.quickeats.dto.CreateOrderDTO;
import com.quickeats.dto.OrderItemDTO;
import com.quickeats.dto.OrderResponseDTO;
import com.quickeats.model.Menu;
import com.quickeats.model.Order;
import com.quickeats.model.Restaurant;
import com.quickeats.model.User;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

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

    public OrderResponseDTO placeOrder(CreateOrderDTO createOrderDTO) {
        User user = userService.getUserById(createOrderDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + createOrderDTO.getUserId()));
        
        Restaurant restaurant = restaurantService.getRestaurantById(createOrderDTO.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + createOrderDTO.getRestaurantId()));

        Double totalAmount = createOrderDTO.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        try {
            String orderItemsJson = objectMapper.writeValueAsString(createOrderDTO.getItems());
            
            Order order = new Order(user, restaurant, totalAmount, orderItemsJson);
            order.setStatus("PENDING");
            
            Order savedOrder = orderRepository.save(order);
            
            return convertToOrderResponseDTO(savedOrder, createOrderDTO.getItems());
            
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing order items", e);
        }
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            Order o = order.get();
            o.setStatus(status);
            return orderRepository.save(o);
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    public List<Order> getOrdersByUserAndStatus(Long userId, String status) {
        return orderRepository.findByUserIdAndStatus(userId, status);
    }

    public List<Order> getOrdersByRestaurantAndStatus(Long restaurantId, String status) {
        return orderRepository.findByRestaurantIdAndStatus(restaurantId, status);
    }

    public List<Order> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersBetweenDates(startDate, endDate);
    }

    public Long getOrdersCountByStatus(String status) {
        return orderRepository.countByStatus(status);
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
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        try {
            List<OrderItemDTO> items = objectMapper.readValue(
                order.getOrderItems(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderItemDTO.class)
            );
            
            return convertToOrderResponseDTO(order, items);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing order items", e);
        }
    }
}
