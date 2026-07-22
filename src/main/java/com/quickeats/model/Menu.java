package com.quickeats.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "menus")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Item name is required")
    @Size(max = 200)
    @Column(nullable = false)
    private String itemName;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false)
    private Double price;

    @Size(max = 1000)
    private String description;

    private Boolean isVeg = false;

    @Enumerated(EnumType.STRING)
    private SpiceLevel spiceLevel;

    @Size(max = 500)
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnore
    private Restaurant restaurant;

    public Menu() {
    }

    public Menu(String itemName, Double price, String description) {
        this.itemName = itemName;
        this.price = price;
        this.description = description;
    }

    public Menu(Restaurant restaurant, String itemName, Double price, String description) {
        this.restaurant = restaurant;
        this.itemName = itemName;
        this.price = price;
        this.description = description;
    }

    public Menu(Restaurant restaurant, String itemName, Double price, String description, Boolean isVeg, SpiceLevel spiceLevel, String tags) {
        this.restaurant = restaurant;
        this.itemName = itemName;
        this.price = price;
        this.description = description;
        this.isVeg = isVeg;
        this.spiceLevel = spiceLevel;
        this.tags = tags;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
