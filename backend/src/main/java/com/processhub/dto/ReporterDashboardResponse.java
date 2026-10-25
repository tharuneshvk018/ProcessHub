package com.processhub.dto;

import java.util.ArrayList;
import java.util.List;

public class ReporterDashboardResponse {

    private long totalReported;
    private long activeIssues;
    private long resolvedIssues;
    private long actionRequiredCount;
    private List<IssueResponse> recentReports = new ArrayList<>();

    public ReporterDashboardResponse() {
    }

    public ReporterDashboardResponse(long totalReported, long activeIssues, long resolvedIssues,
                                     long actionRequiredCount, List<IssueResponse> recentReports) {
        this.totalReported = totalReported;
        this.activeIssues = activeIssues;
        this.resolvedIssues = resolvedIssues;
        this.actionRequiredCount = actionRequiredCount;
        this.recentReports = recentReports != null ? recentReports : new ArrayList<>();
    }

    public long getTotalReported() {
        return totalReported;
    }

    public void setTotalReported(long totalReported) {
        this.totalReported = totalReported;
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

    public long getActionRequiredCount() {
        return actionRequiredCount;
    }

    public void setActionRequiredCount(long actionRequiredCount) {
        this.actionRequiredCount = actionRequiredCount;
    }

    public List<IssueResponse> getRecentReports() {
        return recentReports;
    }

    public void setRecentReports(List<IssueResponse> recentReports) {
        this.recentReports = recentReports;
    }
}
