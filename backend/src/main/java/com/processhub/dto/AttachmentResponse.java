package com.processhub.dto;

import java.time.Instant;
import java.util.UUID;

public class AttachmentResponse {

    private UUID id;
    private UUID issueId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private UUID uploadedById;
    private String uploadedByName;
    private String downloadUrl;
    private Instant createdAt;

    public AttachmentResponse() {
    }

    public AttachmentResponse(UUID id, UUID issueId, String fileName, String contentType,
                              Long fileSize, UUID uploadedById, String uploadedByName,
                              String downloadUrl, Instant createdAt) {
        this.id = id;
        this.issueId = issueId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedById = uploadedById;
        this.uploadedByName = uploadedByName;
        this.downloadUrl = downloadUrl;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(UUID issueId) {
        this.issueId = issueId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public UUID getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(UUID uploadedById) {
        this.uploadedById = uploadedById;
    }

    public String getUploadedByName() {
        return uploadedByName;
    }

    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
