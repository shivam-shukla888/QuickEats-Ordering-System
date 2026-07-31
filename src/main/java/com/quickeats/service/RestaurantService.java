package com.quickeats.service;

import com.quickeats.dto.MenuRequestDTO;
import com.quickeats.dto.MenuResponseDTO;
import com.quickeats.dto.RestaurantRequestDTO;
import com.quickeats.dto.RestaurantResponseDTO;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.Menu;
import com.quickeats.model.Restaurant;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.RestaurantRepository;
import com.quickeats.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private RestaurantResponseDTO enrichWithRating(RestaurantResponseDTO dto) {
        if (dto != null && dto.getId() != null) {
            Double avg = reviewRepository.getAverageRatingForRestaurant(dto.getId());
            Long count = reviewRepository.countReviewsForRestaurant(dto.getId());
            dto.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 4.5);
            dto.setTotalReviews(count != null ? count : 0L);
        }
        return dto;
    }

    public RestaurantResponseDTO createRestaurant(RestaurantRequestDTO dto) {
        Restaurant restaurant = new Restaurant(dto.getName(), dto.getAddress(), dto.getCuisineType());
        Restaurant saved = restaurantRepository.save(restaurant);
        return enrichWithRating(RestaurantResponseDTO.fromEntity(saved));
    }

    public Page<RestaurantResponseDTO> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable)
                .map(RestaurantResponseDTO::fromEntity)
                .map(this::enrichWithRating);
    }

    public List<RestaurantResponseDTO> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(RestaurantResponseDTO::fromEntity)
                .map(this::enrichWithRating)
                .collect(Collectors.toList());
    }

    public RestaurantResponseDTO getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
        return enrichWithRating(RestaurantResponseDTO.fromEntity(restaurant));
    }

    public Optional<Restaurant> getRestaurantEntityById(Long id) {
        return restaurantRepository.findById(id);
    }

    public Page<RestaurantResponseDTO> getRestaurantsByCuisineType(String cuisineType, Pageable pageable) {
        return restaurantRepository.findByCuisineTypeIgnoreCase(cuisineType, pageable)
                .map(RestaurantResponseDTO::fromEntity)
                .map(this::enrichWithRating);
    }

    public List<RestaurantResponseDTO> getRestaurantsByCuisineType(String cuisineType) {
        return restaurantRepository.findByCuisineTypeIgnoreCase(cuisineType).stream()
                .map(RestaurantResponseDTO::fromEntity)
                .map(this::enrichWithRating)
                .collect(Collectors.toList());
    }

    public Page<RestaurantResponseDTO> searchRestaurantsByName(String name, Pageable pageable) {
        return restaurantRepository.findByNameContaining(name, pageable)
                .map(RestaurantResponseDTO::fromEntity)
                .map(this::enrichWithRating);
    }

    public List<RestaurantResponseDTO> searchRestaurantsByName(String name) {
        return restaurantRepository.findByNameContaining(name).stream()
                .map(RestaurantResponseDTO::fromEntity)
                .map(this::enrichWithRating)
                .collect(Collectors.toList());
    }

    public List<String> getAllCuisineTypes() {
        return restaurantRepository.findAllCuisineTypes();
    }

    public MenuResponseDTO addMenuToRestaurant(Long restaurantId, MenuRequestDTO dto) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        Menu menu = new Menu(restaurant, dto.getItemName(), dto.getPrice(), dto.getDescription());
        Menu saved = menuRepository.save(menu);
        return MenuResponseDTO.fromEntity(saved);
    }

    public List<MenuResponseDTO> getMenuByRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found with id: " + restaurantId);
        }
        return menuRepository.findByRestaurantId(restaurantId).stream()
                .map(MenuResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<MenuResponseDTO> getMenuByRestaurant(Long restaurantId, Pageable pageable) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found with id: " + restaurantId);
        }
        return menuRepository.findByRestaurantId(restaurantId, pageable)
                .map(MenuResponseDTO::fromEntity);
    }

    public RestaurantResponseDTO updateRestaurant(Long id, RestaurantRequestDTO dto) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        restaurant.setName(dto.getName());
        restaurant.setAddress(dto.getAddress());
        restaurant.setCuisineType(dto.getCuisineType());

        Restaurant saved = restaurantRepository.save(restaurant);
        return RestaurantResponseDTO.fromEntity(saved);
    }

    public void deleteRestaurant(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new ResourceNotFoundException("Restaurant not found with id: " + id);
        }
        restaurantRepository.deleteById(id);
    }

    public MenuResponseDTO updateMenu(Long menuId, MenuRequestDTO dto) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + menuId));

        menu.setItemName(dto.getItemName());
        menu.setPrice(dto.getPrice());
        menu.setDescription(dto.getDescription());

        Menu saved = menuRepository.save(menu);
        return MenuResponseDTO.fromEntity(saved);
    }

    public void deleteMenu(Long menuId) {
        if (!menuRepository.existsById(menuId)) {
            throw new ResourceNotFoundException("Menu item not found with id: " + menuId);
        }
        menuRepository.deleteById(menuId);
    }
}
