package com.quickeats.dto;

import com.quickeats.model.SpiceLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MenuRequestDTO {

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    private String description;
    private Boolean isVeg = false;
    private SpiceLevel spiceLevel;
    private String tags;

    public MenuRequestDTO() {
    }

    public MenuRequestDTO(String itemName, Double price, String description) {
        this.itemName = itemName;
        this.price = price;
        this.description = description;
    }

    public MenuRequestDTO(String itemName, Double price, String description, Boolean isVeg, SpiceLevel spiceLevel, String tags) {
        this.itemName = itemName;
        this.price = price;
        this.description = description;
        this.isVeg = isVeg;
        this.spiceLevel = spiceLevel;
        this.tags = tags;
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
