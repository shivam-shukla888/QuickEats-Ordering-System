package com.quickeats.dto;

public class RecommendationRequestDTO {
    private Long userId;
    private String craving;

    public RecommendationRequestDTO() {}

    public RecommendationRequestDTO(Long userId, String craving) {
        this.userId = userId;
        this.craving = craving;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCraving() {
        return craving;
    }

    public void setCraving(String craving) {
        this.craving = craving;
    }
}
