package com.processhub.dto;

import java.util.UUID;

public class DepartmentMetricResponse {

    private UUID departmentId;
    private String departmentName;
    private String departmentCode;
    private long totalIssues;
    private long activeIssues;
    private long resolvedIssues;
    private double averageResolutionHours;

    public DepartmentMetricResponse() {
    }

    public DepartmentMetricResponse(UUID departmentId, String departmentName, String departmentCode,
                                    long totalIssues, long activeIssues, long resolvedIssues,
                                    double averageResolutionHours) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.totalIssues = totalIssues;
        this.activeIssues = activeIssues;
        this.resolvedIssues = resolvedIssues;
        this.averageResolutionHours = averageResolutionHours;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public long getTotalIssues() {
        return totalIssues;
    }

    public void setTotalIssues(long totalIssues) {
        this.totalIssues = totalIssues;
    }

    public long getActiveIssues() {
        return activeIssues;
    }

    public void setActiveIssues(long activeIssues) {
        this.activeIssues = activeIssues;
    }

    public long getResolvedIssues() {
        return resolvedIssues;
    }

    public void setResolvedIssues(long resolvedIssues) {
        this.resolvedIssues = resolvedIssues;
    }

    public double getAverageResolutionHours() {
        return averageResolutionHours;
    }

    public void setAverageResolutionHours(double averageResolutionHours) {
        this.averageResolutionHours = averageResolutionHours;
    }
}
