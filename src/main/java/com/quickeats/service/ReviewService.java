package com.quickeats.service;

import com.quickeats.dto.CreateReviewDTO;
import com.quickeats.dto.ReviewResponseDTO;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.Order;
import com.quickeats.model.Restaurant;
import com.quickeats.model.Review;
import com.quickeats.model.User;
import com.quickeats.repository.OrderRepository;
import com.quickeats.repository.RestaurantRepository;
import com.quickeats.repository.ReviewRepository;
import com.quickeats.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public ReviewResponseDTO createReview(Long userId, CreateReviewDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + dto.getOrderId()));

        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + dto.getRestaurantId()));

        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only review your own orders.");
        }

        if (order.getRestaurant() == null || !order.getRestaurant().getId().equals(dto.getRestaurantId())) {
            throw new IllegalArgumentException("Order #" + dto.getOrderId() + " does not belong to restaurant #" + dto.getRestaurantId());
        }

        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "";
        if (!"DELIVERED".equals(status)) {
            throw new IllegalStateException("Reviews can only be submitted for orders with DELIVERED status. Current order status: " + status);
        }

        if (reviewRepository.existsByOrderId(dto.getOrderId())) {
            throw new IllegalStateException("A review has already been submitted for order #" + dto.getOrderId());
        }

        Review review = new Review(user, restaurant, order, dto.getRating(), dto.getComment());
        Review saved = reviewRepository.save(review);

        return ReviewResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsForRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found with id: " + restaurantId);
        }
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRatingSummaryForRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant not found with id: " + restaurantId);
        }
        Double avgRating = reviewRepository.getAverageRatingForRestaurant(restaurantId);
        Long totalReviews = reviewRepository.countReviewsForRestaurant(restaurantId);

        double formattedRating = avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 4.5;
        long total = totalReviews != null ? totalReviews : 0L;

        return Map.of(
                "restaurantId", restaurantId,
                "averageRating", formattedRating,
                "totalReviews", total
        );
    }
}
