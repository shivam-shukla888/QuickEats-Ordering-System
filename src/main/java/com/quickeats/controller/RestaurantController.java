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

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER', 'RESTAURANT', 'OWNER')")
    public ResponseEntity<RestaurantResponseDTO> createRestaurant(@Valid @RequestBody RestaurantRequestDTO restaurantDTO) {
        RestaurantResponseDTO createdRestaurant = restaurantService.createRestaurant(restaurantDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRestaurant);
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantResponseDTO>> getAllRestaurants(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "id") String sort,
            @RequestParam(required = false, defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = (page == null && size == null)
                ? org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE, Sort.by(sortDirection, sort))
                : org.springframework.data.domain.PageRequest.of(page != null ? page : 0, size != null ? size : 10, Sort.by(sortDirection, sort));

        return ResponseEntity.ok(restaurantService.getAllRestaurants(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @GetMapping("/cuisine/{cuisineType}")
    public ResponseEntity<Page<RestaurantResponseDTO>> getRestaurantsByCuisineType(
            @PathVariable String cuisineType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        Pageable pageable = (page == null && size == null)
                ? org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id"))
                : org.springframework.data.domain.PageRequest.of(page != null ? page : 0, size != null ? size : 10, Sort.by("id"));

        return ResponseEntity.ok(restaurantService.getRestaurantsByCuisineType(cuisineType, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<RestaurantResponseDTO>> searchRestaurants(
            @RequestParam String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        Pageable pageable = (page == null && size == null)
                ? org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE, Sort.by("id"))
                : org.springframework.data.domain.PageRequest.of(page != null ? page : 0, size != null ? size : 10, Sort.by("id"));

        return ResponseEntity.ok(restaurantService.searchRestaurantsByName(name, pageable));
    }

    @GetMapping("/cuisines")
    public ResponseEntity<List<String>> getAllCuisineTypes() {
        return ResponseEntity.ok(restaurantService.getAllCuisineTypes());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER', 'RESTAURANT', 'OWNER')")
    public ResponseEntity<RestaurantResponseDTO> updateRestaurant(
            @PathVariable Long id, @Valid @RequestBody RestaurantRequestDTO restaurantDTO) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, restaurantDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER', 'RESTAURANT', 'OWNER')")
    public ResponseEntity<Map<String, String>> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(Map.of("message", "Restaurant deleted successfully"));
    }

    @PostMapping("/{id}/menu")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER', 'RESTAURANT', 'OWNER')")
    public ResponseEntity<MenuResponseDTO> addMenuToRestaurant(
            @PathVariable Long id, @Valid @RequestBody MenuRequestDTO menuDTO) {
        MenuResponseDTO createdMenu = restaurantService.addMenuToRestaurant(id, menuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMenu);
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<?> getMenuByRestaurant(
            @PathVariable Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page == null && size == null) {
            return ResponseEntity.ok(restaurantService.getMenuByRestaurant(id));
        } else {
            int p = page != null ? page : 0;
            int s = size != null ? size : 10;
            Pageable pageable = org.springframework.data.domain.PageRequest.of(p, s, Sort.by("id"));
            return ResponseEntity.ok(restaurantService.getMenuByRestaurant(id, pageable));
        }
    }

    @PutMapping("/menu/{menuId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER', 'RESTAURANT', 'OWNER')")
    public ResponseEntity<MenuResponseDTO> updateMenu(
            @PathVariable Long menuId, @Valid @RequestBody MenuRequestDTO menuDTO) {
        return ResponseEntity.ok(restaurantService.updateMenu(menuId, menuDTO));
    }

    @DeleteMapping("/menu/{menuId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER', 'RESTAURANT', 'OWNER')")
    public ResponseEntity<Map<String, String>> deleteMenu(@PathVariable Long menuId) {
        restaurantService.deleteMenu(menuId);
        return ResponseEntity.ok(Map.of("message", "Menu item deleted successfully"));
    }
}
