package com.quickeats.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDTO {
    
    private Long id;
    private Long userId;
    private String userName;
    private Long restaurantId;
    private String restaurantName;
    private String status;
    private Double totalAmount;
    private LocalDateTime orderTime;
    private List<OrderItemDTO> items;
    private String deliveryAddress;
    private String paymentMethod;
    private Double tipAmount;
    private String instructions;

    public OrderResponseDTO() {}

    public OrderResponseDTO(Long id, Long userId, String userName, Long restaurantId, 
                           String restaurantName, String status, Double totalAmount, 
                           LocalDateTime orderTime, List<OrderItemDTO> items) {
        this(id, userId, userName, restaurantId, restaurantName, status, totalAmount, orderTime, items, null, "ONLINE", 0.0, null);
    }

    public OrderResponseDTO(Long id, Long userId, String userName, Long restaurantId, 
                           String restaurantName, String status, Double totalAmount, 
                           LocalDateTime orderTime, List<OrderItemDTO> items,
                           String deliveryAddress, String paymentMethod, Double tipAmount, String instructions) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderTime = orderTime;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.tipAmount = tipAmount;
        this.instructions = instructions;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Double getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(Double tipAmount) {
        this.tipAmount = tipAmount;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
