package com.processhub.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class SubmitResolutionRequest {

    @NotBlank(message = "Work summary is required")
    @Size(max = 2000, message = "Work summary cannot exceed 2000 characters")
    private String workSummary;

    @Size(max = 1000, message = "Root cause cannot exceed 1000 characters")
    private String rootCause;

    @DecimalMin(value = "0.0", inclusive = true, message = "Labor hours cannot be negative")
    private BigDecimal laborHours;

    @Size(max = 1000, message = "Parts used cannot exceed 1000 characters")
    private String partsUsed;

    @Size(max = 500, message = "Resolution evidence URL cannot exceed 500 characters")
    private String resolutionEvidenceUrl;

    public SubmitResolutionRequest() {
    }

    public SubmitResolutionRequest(String workSummary, String rootCause, BigDecimal laborHours,
                                  String partsUsed, String resolutionEvidenceUrl) {
        this.workSummary = workSummary;
        this.rootCause = rootCause;
        this.laborHours = laborHours;
        this.partsUsed = partsUsed;
        this.resolutionEvidenceUrl = resolutionEvidenceUrl;
    }

    public String getWorkSummary() {
        return workSummary;
    }

    public void setWorkSummary(String workSummary) {
        this.workSummary = workSummary;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public BigDecimal getLaborHours() {
        return laborHours;
    }

    public void setLaborHours(BigDecimal laborHours) {
        this.laborHours = laborHours;
    }

    public String getPartsUsed() {
        return partsUsed;
    }

    public void setPartsUsed(String partsUsed) {
        this.partsUsed = partsUsed;
    }

    public String getResolutionEvidenceUrl() {
        return resolutionEvidenceUrl;
    }

    public void setResolutionEvidenceUrl(String resolutionEvidenceUrl) {
        this.resolutionEvidenceUrl = resolutionEvidenceUrl;
    }
}
