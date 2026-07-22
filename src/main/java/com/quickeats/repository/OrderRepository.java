package com.quickeats.repository;

import com.quickeats.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByRestaurantId(Long restaurantId);
    Page<Order> findByRestaurantId(Long restaurantId, Pageable pageable);

    List<Order> findByUserIdAndStatus(Long userId, String status);
    Page<Order> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    List<Order> findByRestaurantIdAndStatus(Long restaurantId, String status);
    Page<Order> findByRestaurantIdAndStatus(Long restaurantId, String status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.orderTime BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.orderTime BETWEEN :startDate AND :endDate")
    Page<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") String status);
}
