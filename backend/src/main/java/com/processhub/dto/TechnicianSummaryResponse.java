package com.processhub.dto;

import java.util.UUID;

public class TechnicianSummaryResponse {

    private UUID userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private UUID departmentId;
    private String departmentName;
    private long activeAssignedCount;

    public TechnicianSummaryResponse() {
    }

    public TechnicianSummaryResponse(UUID userId, String fullName, String email, String phoneNumber,
                                     UUID departmentId, String departmentName, long activeAssignedCount) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.activeAssignedCount = activeAssignedCount;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public long getActiveAssignedCount() {
        return activeAssignedCount;
    }

    public void setActiveAssignedCount(long activeAssignedCount) {
        this.activeAssignedCount = activeAssignedCount;
    }
}
