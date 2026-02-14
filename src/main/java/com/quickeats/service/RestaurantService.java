package com.quickeats.service;

import com.quickeats.model.Menu;
import com.quickeats.model.Restaurant;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuRepository menuRepository;

    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public Optional<Restaurant> getRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }

    public Menu addMenuToRestaurant(Long restaurantId, Menu menu) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        menu.setRestaurant(restaurant);
        return menuRepository.save(menu);
    }

    public List<Menu> getMenuByRestaurant(Long restaurantId) {
        return menuRepository.findByRestaurantId(restaurantId);
    }
}
