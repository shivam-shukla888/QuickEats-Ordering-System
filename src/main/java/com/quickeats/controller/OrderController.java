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
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            userService.getUserByEmail(auth.getName()).ifPresent(u -> createOrderDTO.setUserId(u.getId()));
        }
        if (createOrderDTO.getUserId() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: User must be authenticated to place an order");
        }
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
        OrderResponseDTO dto = orderService.getOrderResponseDTO(id);
        verifyUserOwnershipOrAdmin(dto.getUserId());
        return ResponseEntity.ok(dto);
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
        verifyUserOwnershipOrAdmin(userId);
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
        verifyUserOwnershipOrAdmin(userId);
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
        verifyAdminOrRestaurantOwner();
        String status = statusMap.get("status");
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long id) {
        verifyAdminOrOrderOwner(id);
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    private void verifyAdminOrOrderOwner(Long orderId) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: User is not authenticated");
        }
        com.quickeats.model.User authUser = userService.getUserByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));

        String role = authUser.getRole() != null ? authUser.getRole().toUpperCase() : "";
        boolean isAdminOrRestaurant = role.equals("ADMIN") || role.equals("RESTAURANT") || role.equals("RESTAURANT_OWNER") || role.equals("OWNER");

        if (isAdminOrRestaurant) {
            return;
        }

        com.quickeats.model.Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        boolean isOrderOwner = order.getUser() != null && order.getUser().getId().equals(authUser.getId());

        if (!isOrderOwner) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You can only cancel your own orders unless you are an ADMIN or Restaurant staff");
        }
    }

    private void verifyUserOwnershipOrAdmin(Long targetUserId) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: User is not authenticated");
        }
        com.quickeats.model.User authUser = userService.getUserByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));

        boolean isAdmin = authUser.getRole() != null && "ADMIN".equalsIgnoreCase(authUser.getRole());
        boolean isOwner = authUser.getId() != null && authUser.getId().equals(targetUserId);

        if (!isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You can only view your own orders unless you are an ADMIN");
        }
    }

    private void verifyAdminOrRestaurantOwner() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: User is not authenticated");
        }
        com.quickeats.model.User authUser = userService.getUserByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));

        String role = authUser.getRole() != null ? authUser.getRole().toUpperCase() : "";
        boolean isAuthorized = role.equals("ADMIN") || role.equals("RESTAURANT") || role.equals("RESTAURANT_OWNER") || role.equals("OWNER");
        if (!isAuthorized) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: Plain CUSTOMER cannot modify order status or cancel orders");
        }
    }
}
