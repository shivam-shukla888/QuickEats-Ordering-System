package com.quickeats.controller;

import com.quickeats.dto.MenuRequestDTO;
import com.quickeats.dto.MenuResponseDTO;
import com.quickeats.dto.RestaurantRequestDTO;
import com.quickeats.dto.RestaurantResponseDTO;
import com.quickeats.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<RestaurantResponseDTO> createRestaurant(@Valid @RequestBody RestaurantRequestDTO restaurantDTO) {
        RestaurantResponseDTO createdRestaurant = restaurantService.createRestaurant(restaurantDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRestaurant);
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantResponseDTO>> getAllRestaurants(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(restaurantService.getAllRestaurants(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @GetMapping("/cuisine/{cuisineType}")
    public ResponseEntity<Page<RestaurantResponseDTO>> getRestaurantsByCuisineType(
            @PathVariable String cuisineType,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(restaurantService.getRestaurantsByCuisineType(cuisineType, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<RestaurantResponseDTO>> searchRestaurants(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(restaurantService.searchRestaurantsByName(name, pageable));
    }

    @GetMapping("/cuisines")
    public ResponseEntity<List<String>> getAllCuisineTypes() {
        return ResponseEntity.ok(restaurantService.getAllCuisineTypes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> updateRestaurant(
            @PathVariable Long id, @Valid @RequestBody RestaurantRequestDTO restaurantDTO) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, restaurantDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(Map.of("message", "Restaurant deleted successfully"));
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<MenuResponseDTO> addMenuToRestaurant(
            @PathVariable Long id, @Valid @RequestBody MenuRequestDTO menuDTO) {
        MenuResponseDTO createdMenu = restaurantService.addMenuToRestaurant(id, menuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMenu);
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuResponseDTO>> getMenuByRestaurant(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getMenuByRestaurant(id));
    }

    @PutMapping("/menu/{menuId}")
    public ResponseEntity<MenuResponseDTO> updateMenu(
            @PathVariable Long menuId, @Valid @RequestBody MenuRequestDTO menuDTO) {
        return ResponseEntity.ok(restaurantService.updateMenu(menuId, menuDTO));
    }

    @DeleteMapping("/menu/{menuId}")
    public ResponseEntity<Map<String, String>> deleteMenu(@PathVariable Long menuId) {
        restaurantService.deleteMenu(menuId);
        return ResponseEntity.ok(Map.of("message", "Menu item deleted successfully"));
    }
}
