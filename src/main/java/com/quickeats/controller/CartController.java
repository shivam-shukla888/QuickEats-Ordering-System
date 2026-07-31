package com.quickeats.controller;

import com.quickeats.dto.AddCartItemDTO;
import com.quickeats.dto.CartDTO;
import com.quickeats.dto.OrderResponseDTO;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.User;
import com.quickeats.service.CartService;
import com.quickeats.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("User must be authenticated to perform cart operations");
        }
        return userService.getUserByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @GetMapping
    public ResponseEntity<CartDTO> getCart() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(cartService.getCartDTO(user.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(@Valid @RequestBody AddCartItemDTO dto) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(cartService.addItemToCart(user.getId(), dto));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateItemQuantity(
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body) {
        User user = getAuthenticatedUser();
        Integer quantity = body.getOrDefault("quantity", 1);
        return ResponseEntity.ok(cartService.updateCartItemQuantity(user.getId(), itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeItemFromCart(@PathVariable Long itemId) {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(cartService.removeItemFromCart(user.getId(), itemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartDTO> clearCart() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(cartService.clearCart(user.getId()));
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDTO> checkoutCart(@RequestBody(required = false) Map<String, String> payload) {
        User user = getAuthenticatedUser();
        String address = payload != null ? payload.get("deliveryAddress") : null;
        String payment = payload != null ? payload.get("paymentMethod") : null;

        if (address == null || address.trim().isEmpty()) {
            address = "123 Main Street";
        }
        if (payment == null || payment.trim().isEmpty()) {
            payment = "CREDIT_CARD";
        }

        return ResponseEntity.ok(cartService.checkoutCart(user.getId(), address, payment));
    }
}
