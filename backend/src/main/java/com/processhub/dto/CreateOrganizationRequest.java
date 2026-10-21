package com.processhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(max = 255, message = "Organization name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Organization code is required")
    @Size(max = 50, message = "Organization code cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Organization code must contain only uppercase letters, numbers, hyphens, and underscores")
    private String code;

    public CreateOrganizationRequest() {
    }

    public CreateOrganizationRequest(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
