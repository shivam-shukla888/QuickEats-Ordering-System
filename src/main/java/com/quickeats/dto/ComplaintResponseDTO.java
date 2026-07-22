package com.quickeats.dto;

import java.util.List;

public class ComplaintResponseDTO {
    private String investigation;
    private String classification;
    private String decision;
    private String actionTaken;
    private String reasoning;
    private List<String> toolsInvoked;

    public ComplaintResponseDTO() {}

    public ComplaintResponseDTO(String investigation, String classification, String decision, String actionTaken, String reasoning, List<String> toolsInvoked) {
        this.investigation = investigation;
        this.classification = classification;
        this.decision = decision;
        this.actionTaken = actionTaken;
        this.reasoning = reasoning;
        this.toolsInvoked = toolsInvoked;
    }

    public String getInvestigation() { return investigation; }
    public void setInvestigation(String investigation) { this.investigation = investigation; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public List<String> getToolsInvoked() { return toolsInvoked; }
    public void setToolsInvoked(List<String> toolsInvoked) { this.toolsInvoked = toolsInvoked; }
}
