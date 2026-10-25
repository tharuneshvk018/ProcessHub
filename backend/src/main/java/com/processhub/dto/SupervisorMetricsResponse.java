package com.processhub.dto;

public class SupervisorMetricsResponse {

    private long pendingReviewCount;
    private long unassignedCount;
    private long awaitingRfiCount;
    private long criticalUrgentCount;
    private long routingExceptionsCount;
    private long totalSupervisedIssues;

    public SupervisorMetricsResponse() {
    }

    public SupervisorMetricsResponse(long pendingReviewCount, long unassignedCount, long awaitingRfiCount,
                                     long criticalUrgentCount, long routingExceptionsCount, long totalSupervisedIssues) {
        this.pendingReviewCount = pendingReviewCount;
        this.unassignedCount = unassignedCount;
        this.awaitingRfiCount = awaitingRfiCount;
        this.criticalUrgentCount = criticalUrgentCount;
        this.routingExceptionsCount = routingExceptionsCount;
        this.totalSupervisedIssues = totalSupervisedIssues;
    }

    public long getPendingReviewCount() {
        return pendingReviewCount;
    }

    public void setPendingReviewCount(long pendingReviewCount) {
        this.pendingReviewCount = pendingReviewCount;
    }

    public long getUnassignedCount() {
        return unassignedCount;
    }

    public void setUnassignedCount(long unassignedCount) {
        this.unassignedCount = unassignedCount;
    }

    public long getAwaitingRfiCount() {
        return awaitingRfiCount;
    }

    public void setAwaitingRfiCount(long awaitingRfiCount) {
        this.awaitingRfiCount = awaitingRfiCount;
    }

    public long getCriticalUrgentCount() {
        return criticalUrgentCount;
    }

    public void setCriticalUrgentCount(long criticalUrgentCount) {
        this.criticalUrgentCount = criticalUrgentCount;
    }

    public long getRoutingExceptionsCount() {
        return routingExceptionsCount;
    }

    public void setRoutingExceptionsCount(long routingExceptionsCount) {
        this.routingExceptionsCount = routingExceptionsCount;
    }

    public long getTotalSupervisedIssues() {
        return totalSupervisedIssues;
    }

    public void setTotalSupervisedIssues(long totalSupervisedIssues) {
        this.totalSupervisedIssues = totalSupervisedIssues;
    }
}
