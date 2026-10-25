package com.processhub.dto;

import java.time.Instant;
import java.util.UUID;

public class RecurrenceSummaryResponse {

    private UUID id;
    private String referenceCode;
    private String title;
    private String status;
    private Instant createdAt;
    private Instant resolvedAt;
    private String assignedTechnicianName;
    private String workSummary;

    public RecurrenceSummaryResponse() {
    }

    public RecurrenceSummaryResponse(UUID id, String referenceCode, String title, String status,
                                     Instant createdAt, Instant resolvedAt,
                                     String assignedTechnicianName, String workSummary) {
        this.id = id;
        this.referenceCode = referenceCode;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
        this.assignedTechnicianName = assignedTechnicianName;
        this.workSummary = workSummary;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getAssignedTechnicianName() {
        return assignedTechnicianName;
    }

    public void setAssignedTechnicianName(String assignedTechnicianName) {
        this.assignedTechnicianName = assignedTechnicianName;
    }

    public String getWorkSummary() {
        return workSummary;
    }

    public void setWorkSummary(String workSummary) {
        this.workSummary = workSummary;
    }
}
