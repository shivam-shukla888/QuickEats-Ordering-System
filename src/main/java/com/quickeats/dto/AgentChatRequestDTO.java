package com.quickeats.dto;

public class AgentChatRequestDTO {
    private Long userId;
    private String message;

    public AgentChatRequestDTO() {}

    public AgentChatRequestDTO(Long userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
