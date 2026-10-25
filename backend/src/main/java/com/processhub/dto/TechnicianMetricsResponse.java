package com.processhub.dto;

public class TechnicianMetricsResponse {

    private long assignedCount;
    private long inProgressCount;
    private long awaitingFinanceCount;
    private long resolvedPendingVerificationCount;
    private long completedCount;

    public TechnicianMetricsResponse() {
    }

    public TechnicianMetricsResponse(long assignedCount, long inProgressCount,
                                     long awaitingFinanceCount, long resolvedPendingVerificationCount,
                                     long completedCount) {
        this.assignedCount = assignedCount;
        this.inProgressCount = inProgressCount;
        this.awaitingFinanceCount = awaitingFinanceCount;
        this.resolvedPendingVerificationCount = resolvedPendingVerificationCount;
        this.completedCount = completedCount;
    }

    public long getAssignedCount() {
        return assignedCount;
    }

    public void setAssignedCount(long assignedCount) {
        this.assignedCount = assignedCount;
    }

    public long getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(long inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public long getAwaitingFinanceCount() {
        return awaitingFinanceCount;
    }

    public void setAwaitingFinanceCount(long awaitingFinanceCount) {
        this.awaitingFinanceCount = awaitingFinanceCount;
    }

    public long getResolvedPendingVerificationCount() {
        return resolvedPendingVerificationCount;
    }

    public void setResolvedPendingVerificationCount(long resolvedPendingVerificationCount) {
        this.resolvedPendingVerificationCount = resolvedPendingVerificationCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(long completedCount) {
        this.completedCount = completedCount;
    }
}
