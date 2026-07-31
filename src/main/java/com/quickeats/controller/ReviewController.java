package com.quickeats.controller;

import com.quickeats.dto.CreateReviewDTO;
import com.quickeats.dto.ReviewResponseDTO;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.User;
import com.quickeats.service.ReviewService;
import com.quickeats.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("User must be authenticated to submit a review");
        }
        return userService.getUserByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }

    @PostMapping("/api/reviews")
    public ResponseEntity<ReviewResponseDTO> createReview(@Valid @RequestBody CreateReviewDTO dto) {
        User user = getAuthenticatedUser();
        ReviewResponseDTO response = reviewService.createReview(user.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/restaurants/{restaurantId}/reviews")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsForRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewService.getReviewsForRestaurant(restaurantId));
    }

    @GetMapping("/api/restaurants/{restaurantId}/rating")
    public ResponseEntity<Map<String, Object>> getRatingForRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewService.getRatingSummaryForRestaurant(restaurantId));
    }
}
