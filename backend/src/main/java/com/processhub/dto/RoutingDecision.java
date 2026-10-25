package com.processhub.dto;

import com.processhub.domain.entity.User;

public class RoutingDecision {

    private String stage;
    private String responsibleRole;
    private String blockingReason;
    private String nextAction;
    private boolean routingException;
    private User assignedSupervisor;

    public RoutingDecision() {
    }

    public RoutingDecision(String stage, String responsibleRole, String blockingReason,
                           String nextAction, boolean routingException, User assignedSupervisor) {
        this.stage = stage;
        this.responsibleRole = responsibleRole;
        this.blockingReason = blockingReason;
        this.nextAction = nextAction;
        this.routingException = routingException;
        this.assignedSupervisor = assignedSupervisor;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getResponsibleRole() {
        return responsibleRole;
    }

    public void setResponsibleRole(String responsibleRole) {
        this.responsibleRole = responsibleRole;
    }

    public String getBlockingReason() {
        return blockingReason;
    }

    public void setBlockingReason(String blockingReason) {
        this.blockingReason = blockingReason;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public boolean isRoutingException() {
        return routingException;
    }

    public void setRoutingException(boolean routingException) {
        this.routingException = routingException;
    }

    public User getAssignedSupervisor() {
        return assignedSupervisor;
    }

    public void setAssignedSupervisor(User assignedSupervisor) {
        this.assignedSupervisor = assignedSupervisor;
    }
}
