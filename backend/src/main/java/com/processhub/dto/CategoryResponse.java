package com.processhub.dto;

import java.time.Instant;
import java.util.UUID;

public class CategoryResponse {

    private UUID id;
    private UUID organizationId;
    private UUID departmentId;
    private String departmentName;
    private String code;
    private String name;
    private String description;
    private String subtypes;
    private String defaultPriority;
    private boolean requiresAsset;
    private boolean active;
    private Instant createdAt;

    public CategoryResponse() {
    }

    public CategoryResponse(UUID id, UUID organizationId, UUID departmentId, String departmentName, String code,
                            String name, String description, String subtypes, String defaultPriority,
                            boolean requiresAsset, boolean active, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.code = code;
        this.name = name;
        this.description = description;
        this.subtypes = subtypes;
        this.defaultPriority = defaultPriority;
        this.requiresAsset = requiresAsset;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
