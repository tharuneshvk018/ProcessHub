package com.processhub.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DecideExpenditureRequest {

    @NotNull(message = "Approval decision is required")
    private Boolean approved;

    @Size(max = 1000, message = "Decision notes cannot exceed 1000 characters")
    private String notes;

    public DecideExpenditureRequest() {
    }

    public DecideExpenditureRequest(Boolean approved, String notes) {
        this.approved = approved;
        this.notes = notes;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
