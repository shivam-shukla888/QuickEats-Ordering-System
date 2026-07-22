package com.quickeats.dto;

import java.util.List;

public class AgentChatResponseDTO {
    private String response;
    private List<String> toolsInvoked;

    public AgentChatResponseDTO() {}

    public AgentChatResponseDTO(String response, List<String> toolsInvoked) {
        this.response = response;
        this.toolsInvoked = toolsInvoked;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<String> getToolsInvoked() {
        return toolsInvoked;
    }

    public void setToolsInvoked(List<String> toolsInvoked) {
        this.toolsInvoked = toolsInvoked;
    }
}
