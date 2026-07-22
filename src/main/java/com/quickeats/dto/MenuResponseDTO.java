package com.quickeats.dto;

import com.quickeats.model.Menu;

public class MenuResponseDTO {

    private Long id;
    private Long restaurantId;
    private String itemName;
    private Double price;
    private String description;

    public MenuResponseDTO() {
    }

    public MenuResponseDTO(Long id, Long restaurantId, String itemName, Double price, String description) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.itemName = itemName;
        this.price = price;
        this.description = description;
    }

    public static MenuResponseDTO fromEntity(Menu menu) {
        if (menu == null) {
            return null;
        }
        Long rId = menu.getRestaurant() != null ? menu.getRestaurant().getId() : null;
        return new MenuResponseDTO(
            menu.getId(),
            rId,
            menu.getItemName(),
            menu.getPrice(),
            menu.getDescription()
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
}
