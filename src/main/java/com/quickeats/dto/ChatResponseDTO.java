package com.quickeats.dto;

import java.util.List;

public class ChatResponseDTO {
    private String reply;
    private List<MenuResponseDTO> recommendations;

    public ChatResponseDTO() {
    }

    public ChatResponseDTO(String reply, List<MenuResponseDTO> recommendations) {
        this.reply = reply;
        this.recommendations = recommendations;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<MenuResponseDTO> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<MenuResponseDTO> recommendations) {
        this.recommendations = recommendations;
    }
}
