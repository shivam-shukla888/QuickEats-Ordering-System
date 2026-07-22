package com.quickeats.observability;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_call_logs")
public class AgentCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String agentType; // ORDERING, RAG, SUPPORT, ORCHESTRATOR

    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String inputPrompt;

    @Column(columnDefinition = "TEXT")
    private String toolsInvoked;

    @Column(nullable = false)
    private Long latencyMs;

    private Integer tokenUsage;

    @Column(nullable = false)
    private boolean success;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public AgentCallLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AgentCallLog(String agentType, Long userId, String inputPrompt, String toolsInvoked, Long latencyMs, Integer tokenUsage, boolean success, String errorMessage) {
        this.agentType = agentType;
        this.userId = userId;
        this.inputPrompt = inputPrompt;
        this.toolsInvoked = toolsInvoked;
        this.latencyMs = latencyMs;
        this.tokenUsage = tokenUsage;
        this.success = success;
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getInputPrompt() { return inputPrompt; }
    public void setInputPrompt(String inputPrompt) { this.inputPrompt = inputPrompt; }

    public String getToolsInvoked() { return toolsInvoked; }
    public void setToolsInvoked(String toolsInvoked) { this.toolsInvoked = toolsInvoked; }

    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }

    public Integer getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(Integer tokenUsage) { this.tokenUsage = tokenUsage; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
