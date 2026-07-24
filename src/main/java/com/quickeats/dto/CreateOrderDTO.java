package com.quickeats.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateOrderDTO {
    
    private Long userId;
    
    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
    
    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemDTO> items;

    public CreateOrderDTO() {}

    public CreateOrderDTO(Long restaurantId, List<OrderItemDTO> items) {
        this.restaurantId = restaurantId;
        this.items = items;
    }

    public CreateOrderDTO(Long userId, Long restaurantId, List<OrderItemDTO> items) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.items = items;
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

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }
}
