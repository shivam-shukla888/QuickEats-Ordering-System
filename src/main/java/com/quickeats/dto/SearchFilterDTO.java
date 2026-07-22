package com.quickeats.dto;

import com.quickeats.model.SpiceLevel;
import java.util.List;

public class SearchFilterDTO {
    private String restaurantName;
    private String cuisineType;
    private Double minPrice;
    private Double maxPrice;
    private Boolean isVeg;
    private SpiceLevel spiceLevel;
    private String addressKeywords;
    private List<String> keywords;

    public SearchFilterDTO() {
    }

    public SearchFilterDTO(String restaurantName, String cuisineType, Double minPrice, Double maxPrice, Boolean isVeg, SpiceLevel spiceLevel, String addressKeywords, List<String> keywords) {
        this.restaurantName = restaurantName;
        this.cuisineType = cuisineType;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.isVeg = isVeg;
        this.spiceLevel = spiceLevel;
        this.addressKeywords = addressKeywords;
        this.keywords = keywords;
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

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
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

    public String getAddressKeywords() {
        return addressKeywords;
    }

    public void setAddressKeywords(String addressKeywords) {
        this.addressKeywords = addressKeywords;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
