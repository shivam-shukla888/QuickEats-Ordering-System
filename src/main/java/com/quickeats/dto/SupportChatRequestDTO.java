package com.quickeats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SupportChatRequestDTO {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "message is required")
    private String message;

    public SupportChatRequestDTO() {
    }

    public SupportChatRequestDTO(Long userId, String message) {
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
