package com.quickeats.controller;

import com.quickeats.dto.CreateOrderDTO;
import com.quickeats.dto.OrderResponseDTO;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private com.quickeats.service.UserService userService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> placeOrder(@Valid @RequestBody CreateOrderDTO createOrderDTO) {
        OrderResponseDTO orderResponse = orderService.placeOrder(createOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderResponseDTO(id));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderResponseDTO>> getMyOrders(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        com.quickeats.model.User user = userService.getUserByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
        return ResponseEntity.ok(orderService.getOrdersByUser(user.getId(), pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId, pageable));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByRestaurant(
            @PathVariable Long restaurantId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId, pageable));
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByUserAndStatus(
            @PathVariable Long userId,
            @PathVariable String status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByUserAndStatus(userId, status, pageable));
    }

    @GetMapping("/restaurant/{restaurantId}/status/{status}")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByRestaurantAndStatus(
            @PathVariable Long restaurantId,
            @PathVariable String status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurantAndStatus(restaurantId, status, pageable));
    }

    @GetMapping("/between-dates")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersBetweenDates(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            return ResponseEntity.ok(orderService.getOrdersBetweenDates(start, end, pageable));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO format (e.g. 2026-07-22T10:00:00)");
        }
    }

    @GetMapping("/status/{status}/count")
    public ResponseEntity<Long> getOrdersCountByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderService.getOrdersCountByStatus(status));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> getOrderStatus(@PathVariable Long id) {
        OrderResponseDTO dto = orderService.getOrderResponseDTO(id);
        return ResponseEntity.ok(Map.of("status", dto.getStatus()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        String status = statusMap.get("status");
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
