package com.processhub.dto;

import com.processhub.domain.enums.Role;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateMembershipRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private UUID departmentId;

    @NotNull(message = "Role is required")
    private Role role;

    public CreateMembershipRequest() {
    }

    public CreateMembershipRequest(UUID userId, UUID departmentId, Role role) {
        this.userId = userId;
        this.departmentId = departmentId;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
