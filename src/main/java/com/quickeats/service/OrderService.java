package com.quickeats.service;

import com.quickeats.dto.CreateOrderDTO;
import com.quickeats.dto.OrderItemDTO;
import com.quickeats.dto.OrderResponseDTO;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.Order;
import com.quickeats.model.Restaurant;
import com.quickeats.model.User;
import com.quickeats.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    public OrderResponseDTO placeOrder(CreateOrderDTO createOrderDTO) {
        User user = userService.getUserById(createOrderDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + createOrderDTO.getUserId()));

        Restaurant restaurant = restaurantService.getRestaurantEntityById(createOrderDTO.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + createOrderDTO.getRestaurantId()));

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

    public OrderResponseDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        order.setStatus(status);
        Order updated = orderRepository.save(order);
        return convertToOrderResponseDTO(updated);
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
