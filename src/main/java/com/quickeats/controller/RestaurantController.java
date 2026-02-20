package com.quickeats.controller;

import com.quickeats.model.Menu;
import com.quickeats.model.Restaurant;
import com.quickeats.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<?> createRestaurant(@Valid @RequestBody Restaurant restaurant) {
        try {
            Restaurant createdRestaurant = restaurantService.createRestaurant(restaurant);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRestaurant);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
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

    @GetMapping("/cuisine/{cuisineType}")
    public ResponseEntity<List<Restaurant>> getRestaurantsByCuisineType(@PathVariable String cuisineType) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByCuisineType(cuisineType));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Restaurant>> searchRestaurants(@RequestParam String name) {
        return ResponseEntity.ok(restaurantService.searchRestaurantsByName(name));
    }

    @GetMapping("/cuisines")
    public ResponseEntity<List<String>> getAllCuisineTypes() {
        return ResponseEntity.ok(restaurantService.getAllCuisineTypes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRestaurant(@PathVariable Long id, @Valid @RequestBody Restaurant restaurant) {
        try {
            Restaurant updatedRestaurant = restaurantService.updateRestaurant(id, restaurant);
            return ResponseEntity.ok(updatedRestaurant);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRestaurant(@PathVariable Long id) {
        try {
            restaurantService.deleteRestaurant(id);
            return ResponseEntity.ok(Map.of("message", "Restaurant deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<?> addMenuToRestaurant(@PathVariable Long id, @Valid @RequestBody Menu menu) {
        try {
            Menu createdMenu = restaurantService.addMenuToRestaurant(id, menu);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMenu);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<Menu>> getMenuByRestaurant(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(restaurantService.getMenuByRestaurant(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/menu/{menuId}")
    public ResponseEntity<?> updateMenu(@PathVariable Long menuId, @Valid @RequestBody Menu menu) {
        try {
            Menu updatedMenu = restaurantService.updateMenu(menuId, menu);
            return ResponseEntity.ok(updatedMenu);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/menu/{menuId}")
    public ResponseEntity<?> deleteMenu(@PathVariable Long menuId) {
        try {
            restaurantService.deleteMenu(menuId);
            return ResponseEntity.ok(Map.of("message", "Menu item deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
