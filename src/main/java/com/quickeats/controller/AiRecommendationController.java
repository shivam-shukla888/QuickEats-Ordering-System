package com.quickeats.controller;

import com.quickeats.dto.AiRecommendationDTO;
import com.quickeats.service.AiRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class AiRecommendationController {

    @Autowired
    private AiRecommendationService aiRecommendationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AiRecommendationDTO>> getUserRecommendations(@PathVariable Long userId) {
        List<AiRecommendationDTO> recommendations = aiRecommendationService.getUserRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }
}
