package com.quickeats.controller;

import com.quickeats.model.Menu;
import com.quickeats.model.Restaurant;
import com.quickeats.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody Restaurant restaurant) {
        return ResponseEntity.ok(restaurantService.createRestaurant(restaurant));
    }

    @GetMapping
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<Menu> addMenuToRestaurant(@PathVariable Long id, @RequestBody Menu menu) {
        try {
            return ResponseEntity.ok(restaurantService.addMenuToRestaurant(id, menu));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<Menu>> getMenuByRestaurant(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getMenuByRestaurant(id));
    }
}
