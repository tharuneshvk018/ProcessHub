package com.processhub.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperationalAnalyticsResponse {

    private long totalIssuesCount;
    private long activeIssuesCount;
    private long resolvedIssuesCount;
    private long slaBreachedCount;
    private double slaCompliancePercentage;
    private double averageResolutionHours;
    private Map<String, Long> priorityDistribution = new HashMap<>();
    private Map<String, Long> statusDistribution = new HashMap<>();
    private List<DepartmentMetricResponse> departmentMetrics = new ArrayList<>();
    private Map<String, Long> topRecurringAssets = new HashMap<>();
    private BigDecimal totalSpendApproved = BigDecimal.ZERO;
    private BigDecimal totalSpendPending = BigDecimal.ZERO;

    public OperationalAnalyticsResponse() {
    }

    public OperationalAnalyticsResponse(long totalIssuesCount, long activeIssuesCount, long resolvedIssuesCount,
                                        long slaBreachedCount, double slaCompliancePercentage,
                                        double averageResolutionHours, Map<String, Long> priorityDistribution,
                                        Map<String, Long> statusDistribution,
                                        List<DepartmentMetricResponse> departmentMetrics,
                                        Map<String, Long> topRecurringAssets,
                                        BigDecimal totalSpendApproved, BigDecimal totalSpendPending) {
        this.totalIssuesCount = totalIssuesCount;
        this.activeIssuesCount = activeIssuesCount;
        this.resolvedIssuesCount = resolvedIssuesCount;
        this.slaBreachedCount = slaBreachedCount;
        this.slaCompliancePercentage = slaCompliancePercentage;
        this.averageResolutionHours = averageResolutionHours;
        this.priorityDistribution = priorityDistribution != null ? priorityDistribution : new HashMap<>();
        this.statusDistribution = statusDistribution != null ? statusDistribution : new HashMap<>();
        this.departmentMetrics = departmentMetrics != null ? departmentMetrics : new ArrayList<>();
        this.topRecurringAssets = topRecurringAssets != null ? topRecurringAssets : new HashMap<>();
        this.totalSpendApproved = totalSpendApproved != null ? totalSpendApproved : BigDecimal.ZERO;
        this.totalSpendPending = totalSpendPending != null ? totalSpendPending : BigDecimal.ZERO;
    }

    public long getTotalIssuesCount() {
        return totalIssuesCount;
    }

    public void setTotalIssuesCount(long totalIssuesCount) {
        this.totalIssuesCount = totalIssuesCount;
    }

    public long getActiveIssuesCount() {
        return activeIssuesCount;
    }

    public void setActiveIssuesCount(long activeIssuesCount) {
        this.activeIssuesCount = activeIssuesCount;
    }

    public long getResolvedIssuesCount() {
        return resolvedIssuesCount;
    }

    public void setResolvedIssuesCount(long resolvedIssuesCount) {
        this.resolvedIssuesCount = resolvedIssuesCount;
    }

    public long getSlaBreachedCount() {
        return slaBreachedCount;
    }

    public void setSlaBreachedCount(long slaBreachedCount) {
        this.slaBreachedCount = slaBreachedCount;
    }

    public double getSlaCompliancePercentage() {
        return slaCompliancePercentage;
    }

    public void setSlaCompliancePercentage(double slaCompliancePercentage) {
        this.slaCompliancePercentage = slaCompliancePercentage;
    }

    public double getAverageResolutionHours() {
        return averageResolutionHours;
    }

    public void setAverageResolutionHours(double averageResolutionHours) {
        this.averageResolutionHours = averageResolutionHours;
    }

    public Map<String, Long> getPriorityDistribution() {
        return priorityDistribution;
    }

    public void setPriorityDistribution(Map<String, Long> priorityDistribution) {
        this.priorityDistribution = priorityDistribution;
    }

    public Map<String, Long> getStatusDistribution() {
        return statusDistribution;
    }

    public void setStatusDistribution(Map<String, Long> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }

    public List<DepartmentMetricResponse> getDepartmentMetrics() {
        return departmentMetrics;
    }

    public void setDepartmentMetrics(List<DepartmentMetricResponse> departmentMetrics) {
        this.departmentMetrics = departmentMetrics;
    }

    public Map<String, Long> getTopRecurringAssets() {
        return topRecurringAssets;
    }

    public void setTopRecurringAssets(Map<String, Long> topRecurringAssets) {
        this.topRecurringAssets = topRecurringAssets;
    }

    public BigDecimal getTotalSpendApproved() {
        return totalSpendApproved;
    }

    public void setTotalSpendApproved(BigDecimal totalSpendApproved) {
        this.totalSpendApproved = totalSpendApproved;
    }

    public BigDecimal getTotalSpendPending() {
        return totalSpendPending;
    }

    public void setTotalSpendPending(BigDecimal totalSpendPending) {
        this.totalSpendPending = totalSpendPending;
    }
}
