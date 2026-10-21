package com.processhub.dto;

import java.time.Instant;
import java.util.UUID;

public class DepartmentResponse {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String code;
    private String description;
    private boolean active;
    private Instant createdAt;

    public DepartmentResponse() {
    }

    public DepartmentResponse(UUID id, UUID organizationId, String name, String code, String description, boolean active, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.code = code;
        this.description = description;
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
