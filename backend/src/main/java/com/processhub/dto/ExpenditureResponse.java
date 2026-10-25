package com.processhub.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ExpenditureResponse {

    private UUID id;
    private UUID issueId;
    private String issueReferenceCode;
    private UUID requestedById;
    private String requestedByName;
    private BigDecimal amount;
    private String justification;
    private String status;
    private UUID decidedById;
    private String decidedByName;
    private String decisionNotes;
    private Instant createdAt;
    private Instant decidedAt;

    public ExpenditureResponse() {
    }

    public ExpenditureResponse(UUID id, UUID issueId, String issueReferenceCode, UUID requestedById,
                               String requestedByName, BigDecimal amount, String justification,
                               String status, UUID decidedById, String decidedByName,
                               String decisionNotes, Instant createdAt, Instant decidedAt) {
        this.id = id;
        this.issueId = issueId;
        this.issueReferenceCode = issueReferenceCode;
        this.requestedById = requestedById;
        this.requestedByName = requestedByName;
        this.amount = amount;
        this.justification = justification;
        this.status = status;
        this.decidedById = decidedById;
        this.decidedByName = decidedByName;
        this.decisionNotes = decisionNotes;
        this.createdAt = createdAt;
        this.decidedAt = decidedAt;
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

    public UUID getRequestedById() {
        return requestedById;
    }

    public void setRequestedById(UUID requestedById) {
        this.requestedById = requestedById;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getDecidedById() {
        return decidedById;
    }

    public void setDecidedById(UUID decidedById) {
        this.decidedById = decidedById;
    }

    public String getDecidedByName() {
        return decidedByName;
    }

    public void setDecidedByName(String decidedByName) {
        this.decidedByName = decidedByName;
    }

    public String getDecisionNotes() {
        return decisionNotes;
    }

    public void setDecisionNotes(String decisionNotes) {
        this.decisionNotes = decisionNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }
}
