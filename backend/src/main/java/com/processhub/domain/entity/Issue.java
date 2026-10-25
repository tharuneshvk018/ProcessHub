package com.processhub.domain.entity;

import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "reference_code", nullable = false, unique = true, length = 30)
    private String referenceCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private IssueCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(name = "subtype", length = 100)
    private String subtype;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "location_building", nullable = false, length = 100)
    private String locationBuilding;

    @Column(name = "location_room", nullable = false, length = 100)
    private String locationRoom;

    @Column(name = "asset_id", length = 100)
    private String assetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 50)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private IssueStatus status = IssueStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_technician_id")
    private User assignedTechnician;

    @Column(name = "current_stage", nullable = false, length = 100)
    private String currentStage = "SUBMITTED";

    @Column(name = "blocking_reason", length = 500)
    private String blockingReason;

    @Column(name = "responsible_role", nullable = false, length = 100)
    private String responsibleRole = "ROLE_SUPERVISOR";

    @Column(name = "next_action", nullable = false, length = 500)
    private String nextAction = "Awaiting supervisor triage and assignment";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "work_summary", length = 2000)
    private String workSummary;

    @Column(name = "root_cause", length = 1000)
    private String rootCause;

    @Column(name = "labor_hours", precision = 6, scale = 2)
    private java.math.BigDecimal laborHours;

    @Column(name = "parts_used", length = 1000)
    private String partsUsed;

    @Column(name = "resolution_evidence_url", length = 500)
    private String resolutionEvidenceUrl;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private User verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verification_notes", length = 1000)
    private String verificationNotes;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @Column(name = "sla_breached", nullable = false)
    private boolean slaBreached = false;

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<ExpenditureRequest> expenditureRequests = new ArrayList<>();

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<IssueAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<IssueTimelineEvent> timelineEvents = new ArrayList<>();

    public Issue() {
    }

    public Issue(UUID id, String referenceCode, Organization organization, Department department,
                 IssueCategory category, User reporter, String subtype, String title,
                 String description, String locationBuilding, String locationRoom,
                 String assetId, Priority priority) {
        this.id = id != null ? id : UUID.randomUUID();
        this.referenceCode = referenceCode;
        this.organization = organization;
        this.department = department;
        this.category = category;
        this.reporter = reporter;
        this.subtype = subtype;
        this.title = title;
        this.description = description;
        this.locationBuilding = locationBuilding;
        this.locationRoom = locationRoom;
        this.assetId = assetId;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = IssueStatus.SUBMITTED;
        this.currentStage = "SUBMITTED";
        this.responsibleRole = "ROLE_SUPERVISOR";
        this.nextAction = "Awaiting supervisor triage and assignment";
        this.blockingReason = "Awaiting initial review and assignment by department supervisor";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        long slaHours = switch (this.priority) {
            case CRITICAL -> 4;
            case HIGH -> 12;
            case MEDIUM -> 48;
            case LOW -> 120;
        };
        this.slaDueAt = this.createdAt.plus(slaHours, java.time.temporal.ChronoUnit.HOURS);
        this.slaBreached = false;
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
        if (this.status == null) {
            this.status = IssueStatus.SUBMITTED;
        }
        if (this.priority == null) {
            this.priority = Priority.MEDIUM;
        }
        if (this.currentStage == null) {
            this.currentStage = "SUBMITTED";
        }
        if (this.responsibleRole == null) {
            this.responsibleRole = "ROLE_SUPERVISOR";
        }
        if (this.slaDueAt == null && this.createdAt != null) {
            long hours = switch (this.priority != null ? this.priority : Priority.MEDIUM) {
                case CRITICAL -> 4;
                case HIGH -> 12;
                case MEDIUM -> 48;
                case LOW -> 120;
            };
            this.slaDueAt = this.createdAt.plus(hours, java.time.temporal.ChronoUnit.HOURS);
        }
        if (this.nextAction == null) {
            this.nextAction = "Awaiting supervisor triage and assignment";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addAttachment(IssueAttachment attachment) {
        attachments.add(attachment);
        attachment.setIssue(this);
    }

    public void addTimelineEvent(IssueTimelineEvent event) {
        timelineEvents.add(event);
        event.setIssue(this);
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public IssueCategory getCategory() {
        return category;
    }

    public void setCategory(IssueCategory category) {
        this.category = category;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
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

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public User getAssignedTechnician() {
        return assignedTechnician;
    }

    public void setAssignedTechnician(User assignedTechnician) {
        this.assignedTechnician = assignedTechnician;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public String getBlockingReason() {
        return blockingReason;
    }

    public void setBlockingReason(String blockingReason) {
        this.blockingReason = blockingReason;
    }

    public String getResponsibleRole() {
        return responsibleRole;
    }

    public void setResponsibleRole(String responsibleRole) {
        this.responsibleRole = responsibleRole;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public List<IssueAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<IssueAttachment> attachments) {
        this.attachments = attachments;
    }

    public String getWorkSummary() {
        return workSummary;
    }

    public void setWorkSummary(String workSummary) {
        this.workSummary = workSummary;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public java.math.BigDecimal getLaborHours() {
        return laborHours;
    }

    public void setLaborHours(java.math.BigDecimal laborHours) {
        this.laborHours = laborHours;
    }

    public String getPartsUsed() {
        return partsUsed;
    }

    public void setPartsUsed(String partsUsed) {
        this.partsUsed = partsUsed;
    }

    public String getResolutionEvidenceUrl() {
        return resolutionEvidenceUrl;
    }

    public void setResolutionEvidenceUrl(String resolutionEvidenceUrl) {
        this.resolutionEvidenceUrl = resolutionEvidenceUrl;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public User getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(User verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }

    public List<ExpenditureRequest> getExpenditureRequests() {
        return expenditureRequests;
    }

    public void setExpenditureRequests(List<ExpenditureRequest> expenditureRequests) {
        this.expenditureRequests = expenditureRequests;
    }

    public List<IssueTimelineEvent> getTimelineEvents() {
        return timelineEvents;
    }

    public void setTimelineEvents(List<IssueTimelineEvent> timelineEvents) {
        this.timelineEvents = timelineEvents;
    }

    public Instant getSlaDueAt() {
        return slaDueAt;
    }

    public void setSlaDueAt(Instant slaDueAt) {
        this.slaDueAt = slaDueAt;
    }

    public boolean isSlaBreached() {
        return slaBreached;
    }

    public void setSlaBreached(boolean slaBreached) {
        this.slaBreached = slaBreached;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return Objects.equals(id, issue.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
