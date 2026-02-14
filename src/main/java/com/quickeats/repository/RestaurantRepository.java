package com.quickeats.repository;

import com.quickeats.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    // Custom query methods can be added here if needed, e.g., findByCuisineType
}
