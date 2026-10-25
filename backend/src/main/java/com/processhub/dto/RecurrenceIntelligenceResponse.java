package com.processhub.dto;

import java.util.ArrayList;
import java.util.List;

public class RecurrenceIntelligenceResponse {

    private long totalRecurrences;
    private long assetMatchCount;
    private long locationMatchCount;
    private String recurrenceLevel;
    private boolean chronic;
    private String recommendation;
    private List<RecurrenceSummaryResponse> relatedIssues = new ArrayList<>();

    public RecurrenceIntelligenceResponse() {
    }

    public RecurrenceIntelligenceResponse(long totalRecurrences, long assetMatchCount, long locationMatchCount,
                                          String recurrenceLevel, boolean chronic, String recommendation,
                                          List<RecurrenceSummaryResponse> relatedIssues) {
        this.totalRecurrences = totalRecurrences;
        this.assetMatchCount = assetMatchCount;
        this.locationMatchCount = locationMatchCount;
        this.recurrenceLevel = recurrenceLevel;
        this.chronic = chronic;
        this.recommendation = recommendation;
        this.relatedIssues = relatedIssues != null ? relatedIssues : new ArrayList<>();
    }

    public long getTotalRecurrences() {
        return totalRecurrences;
    }

    public void setTotalRecurrences(long totalRecurrences) {
        this.totalRecurrences = totalRecurrences;
    }

    public long getAssetMatchCount() {
        return assetMatchCount;
    }

    public void setAssetMatchCount(long assetMatchCount) {
        this.assetMatchCount = assetMatchCount;
    }

    public long getLocationMatchCount() {
        return locationMatchCount;
    }

    public void setLocationMatchCount(long locationMatchCount) {
        this.locationMatchCount = locationMatchCount;
    }

    public String getRecurrenceLevel() {
        return recurrenceLevel;
    }

    public void setRecurrenceLevel(String recurrenceLevel) {
        this.recurrenceLevel = recurrenceLevel;
    }

    public boolean isChronic() {
        return chronic;
    }

    public void setChronic(boolean chronic) {
        this.chronic = chronic;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public List<RecurrenceSummaryResponse> getRelatedIssues() {
        return relatedIssues;
    }

    public void setRelatedIssues(List<RecurrenceSummaryResponse> relatedIssues) {
        this.relatedIssues = relatedIssues;
    }
}
