package com.processhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CreateIssueRequest {

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @Size(max = 100, message = "Subtype must not exceed 100 characters")
    private String subtype;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 4000, message = "Description must be between 10 and 4000 characters")
    private String description;

    @NotBlank(message = "Building location is required")
    @Size(max = 100, message = "Building location must not exceed 100 characters")
    private String locationBuilding;

    @NotBlank(message = "Room location is required")
    @Size(max = 100, message = "Room location must not exceed 100 characters")
    private String locationRoom;

    @Size(max = 100, message = "Asset ID must not exceed 100 characters")
    private String assetId;

    private String priority;

    public CreateIssueRequest() {
    }

    public CreateIssueRequest(UUID departmentId, UUID categoryId, String subtype, String title,
                              String description, String locationBuilding, String locationRoom,
                              String assetId, String priority) {
        this.departmentId = departmentId;
        this.categoryId = categoryId;
        this.subtype = subtype;
        this.title = title;
        this.description = description;
        this.locationBuilding = locationBuilding;
        this.locationRoom = locationRoom;
        this.assetId = assetId;
        this.priority = priority;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationBuilding() {
        return locationBuilding;
    }

    public void setLocationBuilding(String locationBuilding) {
        this.locationBuilding = locationBuilding;
    }

    public String getLocationRoom() {
        return locationRoom;
    }

    public void setLocationRoom(String locationRoom) {
        this.locationRoom = locationRoom;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
