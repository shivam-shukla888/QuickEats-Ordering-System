package com.quickeats.dto;

public class SupportChatResponseDTO {

    private String response;

    public SupportChatResponseDTO() {
    }

    public SupportChatResponseDTO(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
