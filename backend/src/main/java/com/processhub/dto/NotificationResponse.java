package com.processhub.dto;

import java.time.Instant;
import java.util.UUID;

public class NotificationResponse {

    private UUID id;
    private UUID orgId;
    private UUID recipientId;
    private UUID issueId;
    private String issueReferenceCode;
    private String notificationType;
    private String typeDisplayName;
    private String title;
    private String message;
    private boolean read;
    private Instant createdAt;
    private Instant readAt;

    public NotificationResponse() {
    }

    public NotificationResponse(UUID id, UUID orgId, UUID recipientId, UUID issueId,
                                String issueReferenceCode, String notificationType,
                                String typeDisplayName, String title, String message,
                                boolean read, Instant createdAt, Instant readAt) {
        this.id = id;
        this.orgId = orgId;
        this.recipientId = recipientId;
        this.issueId = issueId;
        this.issueReferenceCode = issueReferenceCode;
        this.notificationType = notificationType;
        this.typeDisplayName = typeDisplayName;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public void setOrgId(UUID orgId) {
        this.orgId = orgId;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(UUID recipientId) {
        this.recipientId = recipientId;
    }

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(UUID issueId) {
        this.issueId = issueId;
    }

    public String getIssueReferenceCode() {
        return issueReferenceCode;
    }

    public void setIssueReferenceCode(String issueReferenceCode) {
        this.issueReferenceCode = issueReferenceCode;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTypeDisplayName() {
        return typeDisplayName;
    }

    public void setTypeDisplayName(String typeDisplayName) {
        this.typeDisplayName = typeDisplayName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }
}
