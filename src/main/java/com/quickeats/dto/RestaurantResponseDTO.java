package com.quickeats.dto;

import com.quickeats.model.Restaurant;

public class RestaurantResponseDTO {

    private Long id;
    private String name;
    private String address;
    private String cuisineType;
    private Double averageRating = 4.5;
    private Long totalReviews = 0L;

    public RestaurantResponseDTO() {
    }

    public RestaurantResponseDTO(Long id, String name, String address, String cuisineType) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.averageRating = 4.5;
        this.totalReviews = 0L;
    }

    public RestaurantResponseDTO(Long id, String name, String address, String cuisineType, Double averageRating, Long totalReviews) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.averageRating = averageRating != null ? averageRating : 4.5;
        this.totalReviews = totalReviews != null ? totalReviews : 0L;
    }

    public static RestaurantResponseDTO fromEntity(Restaurant restaurant) {
        if (restaurant == null) {
            return null;
        }
        return new RestaurantResponseDTO(
            restaurant.getId(),
            restaurant.getName(),
            restaurant.getAddress(),
            restaurant.getCuisineType()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Long totalReviews) {
        this.totalReviews = totalReviews;
    }
}
