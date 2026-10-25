package com.processhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VerifyResolutionRequest {

    @NotNull(message = "Approval status is required")
    private Boolean approved;

    @NotBlank(message = "Verification notes are required")
    @Size(max = 1000, message = "Verification notes cannot exceed 1000 characters")
    private String verificationNotes;

    public VerifyResolutionRequest() {
    }

    public VerifyResolutionRequest(Boolean approved, String verificationNotes) {
        this.approved = approved;
        this.verificationNotes = verificationNotes;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }
}
