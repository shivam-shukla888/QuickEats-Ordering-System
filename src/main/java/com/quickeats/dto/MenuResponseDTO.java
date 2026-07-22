package com.quickeats.dto;

import com.quickeats.model.Menu;
import com.quickeats.model.SpiceLevel;

public class MenuResponseDTO {

    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private String cuisineType;
    private String itemName;
    private Double price;
    private String description;
    private Boolean isVeg;
    private SpiceLevel spiceLevel;
    private String tags;

    public MenuResponseDTO() {
    }

    public MenuResponseDTO(Long id, Long restaurantId, String restaurantName, String cuisineType, String itemName, Double price, String description, Boolean isVeg, SpiceLevel spiceLevel, String tags) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.cuisineType = cuisineType;
        this.itemName = itemName;
        this.price = price;
        this.description = description;
        this.isVeg = isVeg;
        this.spiceLevel = spiceLevel;
        this.tags = tags;
    }

    public static MenuResponseDTO fromEntity(Menu menu) {
        if (menu == null) {
            return null;
        }
        Long rId = menu.getRestaurant() != null ? menu.getRestaurant().getId() : null;
        String rName = menu.getRestaurant() != null ? menu.getRestaurant().getName() : null;
        String cuisine = menu.getRestaurant() != null ? menu.getRestaurant().getCuisineType() : null;

        return new MenuResponseDTO(
            menu.getId(),
            rId,
            rName,
            cuisine,
            menu.getItemName(),
            menu.getPrice(),
            menu.getDescription(),
            menu.getIsVeg(),
            menu.getSpiceLevel(),
            menu.getTags()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsVeg() {
        return isVeg;
    }

    public void setIsVeg(Boolean isVeg) {
        this.isVeg = isVeg;
    }

    public SpiceLevel getSpiceLevel() {
        return spiceLevel;
    }

    public void setSpiceLevel(SpiceLevel spiceLevel) {
        this.spiceLevel = spiceLevel;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
