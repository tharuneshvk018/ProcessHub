package com.processhub.dto;

public class ExplainableProgressResponse {

    private String currentStage;
    private String stageDisplayName;
    private String blockingReason;
    private String responsibleRole;
    private String nextAction;
    private boolean blocked;

    public ExplainableProgressResponse() {
    }

    public ExplainableProgressResponse(String currentStage, String stageDisplayName, String blockingReason,
                                       String responsibleRole, String nextAction, boolean blocked) {
        this.currentStage = currentStage;
        this.stageDisplayName = stageDisplayName;
        this.blockingReason = blockingReason;
        this.responsibleRole = responsibleRole;
        this.nextAction = nextAction;
        this.blocked = blocked;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public String getStageDisplayName() {
        return stageDisplayName;
    }

    public void setStageDisplayName(String stageDisplayName) {
        this.stageDisplayName = stageDisplayName;
    }

    public String getBlockingReason() {
        return blockingReason;
    }

    public void setBlockingReason(String blockingReason) {
        this.blockingReason = blockingReason;
    }

    public String getResponsibleRole() {
        return responsibleRole;
    }

    public void setResponsibleRole(String responsibleRole) {
        this.responsibleRole = responsibleRole;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
