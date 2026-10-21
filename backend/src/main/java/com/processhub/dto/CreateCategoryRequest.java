package com.processhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CreateCategoryRequest {

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    @NotBlank(message = "Category code is required")
    @Size(max = 50, message = "Category code cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Category code must contain only uppercase letters, numbers, hyphens, and underscores")
    private String code;

    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name cannot exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 1000, message = "Subtypes cannot exceed 1000 characters")
    private String subtypes;

    private String defaultPriority = "MEDIUM";

    private boolean requiresAsset = false;

    public CreateCategoryRequest() {
    }

    public CreateCategoryRequest(UUID departmentId, String code, String name, String description,
                                 String subtypes, String defaultPriority, boolean requiresAsset) {
        this.departmentId = departmentId;
        this.code = code;
        this.name = name;
        this.description = description;
        this.subtypes = subtypes;
        this.defaultPriority = defaultPriority != null ? defaultPriority : "MEDIUM";
        this.requiresAsset = requiresAsset;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubtypes() {
        return subtypes;
    }

    public void setSubtypes(String subtypes) {
        this.subtypes = subtypes;
    }

    public String getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(String defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    public boolean isRequiresAsset() {
        return requiresAsset;
    }

    public void setRequiresAsset(boolean requiresAsset) {
        this.requiresAsset = requiresAsset;
    }
}
