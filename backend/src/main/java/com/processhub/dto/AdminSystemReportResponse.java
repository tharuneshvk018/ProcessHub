package com.processhub.dto;

import java.util.List;
import java.util.Map;

public class AdminSystemReportResponse {

    private long totalIssues;
    private long pendingReviews;
    private long assignedAndActive;
    private long awaitingFinance;
    private long awaitingVerification;
    private long verifiedAndClosed;
    private long rejectedIssues;
    private long reopenedIssues;
    private long slaBreachedCount;
    private double slaComplianceRate;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> priorityDistribution;
    private Map<String, Long> departmentDistribution;
    private List<DepartmentMetricResponse> departmentMetrics;

    public AdminSystemReportResponse() {
    }

    public AdminSystemReportResponse(long totalIssues, long pendingReviews, long assignedAndActive,
                                   long awaitingFinance, long awaitingVerification, long verifiedAndClosed,
                                   long rejectedIssues, long reopenedIssues, long slaBreachedCount,
                                   double slaComplianceRate, Map<String, Long> statusDistribution,
                                   Map<String, Long> priorityDistribution, Map<String, Long> departmentDistribution,
                                   List<DepartmentMetricResponse> departmentMetrics) {
        this.totalIssues = totalIssues;
        this.pendingReviews = pendingReviews;
        this.assignedAndActive = assignedAndActive;
        this.awaitingFinance = awaitingFinance;
        this.awaitingVerification = awaitingVerification;
        this.verifiedAndClosed = verifiedAndClosed;
        this.rejectedIssues = rejectedIssues;
        this.reopenedIssues = reopenedIssues;
        this.slaBreachedCount = slaBreachedCount;
        this.slaComplianceRate = slaComplianceRate;
        this.statusDistribution = statusDistribution;
        this.priorityDistribution = priorityDistribution;
        this.departmentDistribution = departmentDistribution;
        this.departmentMetrics = departmentMetrics;
    }

    public long getTotalIssues() {
        return totalIssues;
    }

    public void setTotalIssues(long totalIssues) {
        this.totalIssues = totalIssues;
    }

    public long getPendingReviews() {
        return pendingReviews;
    }

    public void setPendingReviews(long pendingReviews) {
        this.pendingReviews = pendingReviews;
    }

    public long getAssignedAndActive() {
        return assignedAndActive;
    }

    public void setAssignedAndActive(long assignedAndActive) {
        this.assignedAndActive = assignedAndActive;
    }

    public long getAwaitingFinance() {
        return awaitingFinance;
    }

    public void setAwaitingFinance(long awaitingFinance) {
        this.awaitingFinance = awaitingFinance;
    }

    public long getAwaitingVerification() {
        return awaitingVerification;
    }

    public void setAwaitingVerification(long awaitingVerification) {
        this.awaitingVerification = awaitingVerification;
    }

    public long getVerifiedAndClosed() {
        return verifiedAndClosed;
    }

    public void setVerifiedAndClosed(long verifiedAndClosed) {
        this.verifiedAndClosed = verifiedAndClosed;
    }

    public long getRejectedIssues() {
        return rejectedIssues;
    }

    public void setRejectedIssues(long rejectedIssues) {
        this.rejectedIssues = rejectedIssues;
    }

    public long getReopenedIssues() {
        return reopenedIssues;
    }

    public void setReopenedIssues(long reopenedIssues) {
        this.reopenedIssues = reopenedIssues;
    }

    public long getSlaBreachedCount() {
        return slaBreachedCount;
    }

    public void setSlaBreachedCount(long slaBreachedCount) {
        this.slaBreachedCount = slaBreachedCount;
    }

    public double getSlaComplianceRate() {
        return slaComplianceRate;
    }

    public void setSlaComplianceRate(double slaComplianceRate) {
        this.slaComplianceRate = slaComplianceRate;
    }

    public Map<String, Long> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(Map<String, Long> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }

    public Map<String, Long> getPriorityDistribution() {
        return priorityDistribution;
    }

    public void setPriorityDistribution(Map<String, Long> priorityDistribution) {
        this.priorityDistribution = priorityDistribution;
    }

    public Map<String, Long> getDepartmentDistribution() {
        return departmentDistribution;
    }

    public void setDepartmentDistribution(Map<String, Long> departmentDistribution) {
        this.departmentDistribution = departmentDistribution;
    }

    public List<DepartmentMetricResponse> getDepartmentMetrics() {
        return departmentMetrics;
    }

    public void setDepartmentMetrics(List<DepartmentMetricResponse> departmentMetrics) {
        this.departmentMetrics = departmentMetrics;
    }
}
