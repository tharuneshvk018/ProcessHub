package com.processhub.dto;

import java.time.Instant;
import java.util.UUID;

public class AdminActivityResponse {

    private UUID id;
    private UUID issueId;
    private String issueReferenceCode;
    private String issueTitle;
    private UUID actorId;
    private String actorName;
    private String actorEmail;
    private String actorRole;
    private String action;
    private String fromStatus;
    private String toStatus;
    private String notes;
    private Instant timestamp;

    public AdminActivityResponse() {
    }

    public AdminActivityResponse(UUID id, UUID issueId, String issueReferenceCode, String issueTitle,
                                 UUID actorId, String actorName, String actorEmail, String actorRole,
                                 String action, String fromStatus, String toStatus, String notes, Instant timestamp) {
        this.id = id;
        this.issueId = issueId;
        this.issueReferenceCode = issueReferenceCode;
        this.issueTitle = issueTitle;
        this.actorId = actorId;
        this.actorName = actorName;
        this.actorEmail = actorEmail;
        this.actorRole = actorRole;
        this.action = action;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.notes = notes;
        this.timestamp = timestamp;
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

    public String getIssueReferenceCode() {
        return issueReferenceCode;
    }

    public void setIssueReferenceCode(String issueReferenceCode) {
        this.issueReferenceCode = issueReferenceCode;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
