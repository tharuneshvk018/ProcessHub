package com.processhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(max = 255, message = "Department name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Department code is required")
    @Size(max = 50, message = "Department code cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Department code must contain only uppercase letters, numbers, hyphens, and underscores")
    private String code;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    public CreateDepartmentRequest() {
    }

    public CreateDepartmentRequest(String name, String code, String description) {
        this.name = name;
        this.code = code;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
