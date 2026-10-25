package com.processhub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateProgressRequest {

    @NotBlank(message = "Progress notes are required")
    @Size(max = 2000, message = "Progress notes cannot exceed 2000 characters")
    private String notes;

    @Min(value = 0, message = "Progress percentage must be between 0 and 100")
    @Max(value = 100, message = "Progress percentage must be between 0 and 100")
    private Integer progressPercentage;

    public UpdateProgressRequest() {
    }

    public UpdateProgressRequest(String notes, Integer progressPercentage) {
        this.notes = notes;
        this.progressPercentage = progressPercentage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
}
