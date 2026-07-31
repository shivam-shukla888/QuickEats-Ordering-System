package com.quickeats.dto;

import com.quickeats.model.Cart;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CartDTO {

    private Long id;
    private Long userId;
    private Long restaurantId;
    private List<CartItemDTO> items;
    private Double totalAmount;
    private LocalDateTime updatedAt;

    public CartDTO() {
    }

    public static CartDTO fromEntity(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        dto.setRestaurantId(cart.getRestaurantId());
        dto.setTotalAmount(cart.getTotalAmount());
        dto.setUpdatedAt(cart.getUpdatedAt());
        if (cart.getItems() != null) {
            dto.setItems(cart.getItems().stream()
                    .map(CartItemDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
