package com.processhub.dto;

import com.processhub.domain.enums.Role;
import java.time.Instant;
import java.util.UUID;

public class MembershipResponse {

    private UUID id;
    private UUID organizationId;
    private String organizationName;
    private UUID userId;
    private String userEmail;
    private String userFullName;
    private UUID departmentId;
    private String departmentName;
    private Role role;
    private boolean active;
    private Instant createdAt;

    public MembershipResponse() {
    }

    public MembershipResponse(UUID id, UUID organizationId, String organizationName, UUID userId, String userEmail,
                              String userFullName, UUID departmentId, String departmentName, Role role, boolean active, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFullName = userFullName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.role = role;
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

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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
