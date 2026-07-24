package com.quickeats.dto;

public class AiRecommendationDTO {

    private String restaurantName;
    private String itemName;
    private String reason;

    public AiRecommendationDTO() {
    }

    public AiRecommendationDTO(String restaurantName, String itemName, String reason) {
        this.restaurantName = restaurantName;
        this.itemName = itemName;
        this.reason = reason;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
