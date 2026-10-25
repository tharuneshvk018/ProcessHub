package com.processhub.dto;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public class ApproveIssueRequest {

    private UUID technicianId;

    @Size(max = 1000, message = "Approval notes must not exceed 1000 characters")
    private String notes;

    private String priorityOverride;

    public ApproveIssueRequest() {
    }

    public ApproveIssueRequest(UUID technicianId, String notes, String priorityOverride) {
        this.technicianId = technicianId;
        this.notes = notes;
        this.priorityOverride = priorityOverride;
    }

    public UUID getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(UUID technicianId) {
        this.technicianId = technicianId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPriorityOverride() {
        return priorityOverride;
    }

    public void setPriorityOverride(String priorityOverride) {
        this.priorityOverride = priorityOverride;
    }
}
