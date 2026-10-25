package com.processhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectIssueRequest {

    @NotBlank(message = "Rejection reason is mandatory")
    @Size(min = 10, max = 1000, message = "Rejection reason must be between 10 and 1000 characters")
    private String reason;

    public RejectIssueRequest() {
    }

    public RejectIssueRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
