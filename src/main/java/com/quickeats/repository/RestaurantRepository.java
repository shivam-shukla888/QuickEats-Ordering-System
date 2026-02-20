package com.quickeats.repository;

import com.quickeats.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    List<Restaurant> findByCuisineTypeIgnoreCase(String cuisineType);
    
    @Query("SELECT r FROM Restaurant r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Restaurant> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT DISTINCT r.cuisineType FROM Restaurant r")
    List<String> findAllCuisineTypes();
    
    Optional<Restaurant> findByName(String name);
}
