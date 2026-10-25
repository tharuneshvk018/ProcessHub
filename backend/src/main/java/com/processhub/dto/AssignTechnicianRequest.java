package com.processhub.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class AssignTechnicianRequest {

    @NotNull(message = "Technician ID is required")
    private UUID technicianId;

    @Size(max = 1000, message = "Instructions must not exceed 1000 characters")
    private String instructions;

    public AssignTechnicianRequest() {
    }

    public AssignTechnicianRequest(UUID technicianId, String instructions) {
        this.technicianId = technicianId;
        this.instructions = instructions;
    }

    public UUID getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(UUID technicianId) {
        this.technicianId = technicianId;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
