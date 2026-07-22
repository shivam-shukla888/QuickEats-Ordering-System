package com.quickeats.dto;

import com.quickeats.rag.RecommendationService;

import java.util.List;

public class RecommendationResponseDTO {
    private List<RecommendationService.DishDTO> recommendedDishes;
    private String explanation;
    private List<Double> retrievalScores;

    public RecommendationResponseDTO() {}

    public RecommendationResponseDTO(List<RecommendationService.DishDTO> recommendedDishes, String explanation, List<Double> retrievalScores) {
        this.recommendedDishes = recommendedDishes;
        this.explanation = explanation;
        this.retrievalScores = retrievalScores;
    }

    public List<RecommendationService.DishDTO> getRecommendedDishes() {
        return recommendedDishes;
    }

    public void setRecommendedDishes(List<RecommendationService.DishDTO> recommendedDishes) {
        this.recommendedDishes = recommendedDishes;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<Double> getRetrievalScores() {
        return retrievalScores;
    }

    public void setRetrievalScores(List<Double> retrievalScores) {
        this.retrievalScores = retrievalScores;
    }
}
