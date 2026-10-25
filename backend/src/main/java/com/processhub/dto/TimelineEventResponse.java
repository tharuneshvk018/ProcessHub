package com.processhub.dto;

import java.time.Instant;
import java.util.UUID;

public class TimelineEventResponse {

    private UUID id;
    private UUID actorId;
    private String actorName;
    private String action;
    private String fromStatus;
    private String toStatus;
    private String notes;
    private Instant createdAt;

    public TimelineEventResponse() {
    }

    public TimelineEventResponse(UUID id, UUID actorId, String actorName, String action,
                                 String fromStatus, String toStatus, String notes, Instant createdAt) {
        this.id = id;
        this.actorId = actorId;
        this.actorName = actorName;
        this.action = action;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
