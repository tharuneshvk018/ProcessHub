package com.processhub.dto;

import java.math.BigDecimal;

public class FinanceMetricsResponse {

    private long pendingApprovalCount;
    private BigDecimal pendingTotalAmount;
    private long approvedCount;
    private long rejectedCount;

    public FinanceMetricsResponse() {
    }

    public FinanceMetricsResponse(long pendingApprovalCount, BigDecimal pendingTotalAmount,
                                  long approvedCount, long rejectedCount) {
        this.pendingApprovalCount = pendingApprovalCount;
        this.pendingTotalAmount = pendingTotalAmount != null ? pendingTotalAmount : BigDecimal.ZERO;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
    }

    public long getPendingApprovalCount() {
        return pendingApprovalCount;
    }

    public void setPendingApprovalCount(long pendingApprovalCount) {
        this.pendingApprovalCount = pendingApprovalCount;
    }

    public BigDecimal getPendingTotalAmount() {
        return pendingTotalAmount;
    }

    public void setPendingTotalAmount(BigDecimal pendingTotalAmount) {
        this.pendingTotalAmount = pendingTotalAmount;
    }

    public long getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(long approvedCount) {
        this.approvedCount = approvedCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }
}
