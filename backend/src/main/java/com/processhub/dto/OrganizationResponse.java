package com.processhub.dto;

import java.time.Instant;
import java.util.UUID;

public class OrganizationResponse {

    private UUID id;
    private String name;
    private String code;
    private String status;
    private Instant createdAt;

    public OrganizationResponse() {
    }

    public OrganizationResponse(UUID id, String name, String code, String status, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
