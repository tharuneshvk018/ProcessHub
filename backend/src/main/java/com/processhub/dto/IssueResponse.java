package com.processhub.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IssueResponse {

    private UUID id;
    private String referenceCode;
    private UUID orgId;
    private String orgName;
    private UUID departmentId;
    private String departmentName;
    private UUID categoryId;
    private String categoryName;
    private String categoryCode;
    private String subtype;
    private String title;
    private String description;
    private String locationBuilding;
    private String locationRoom;
    private String assetId;
    private String priority;
    private String status;
    private String statusDisplayName;
    private UUID reporterId;
    private String reporterName;
    private String reporterEmail;
    private UUID assignedTechnicianId;
    private String assignedTechnicianName;
    private String currentStage;
    private String responsibleRole;
    private String nextAction;
    private String blockingReason;
    private ExplainableProgressResponse explainableProgress;
    private String workSummary;
    private String rootCause;
    private java.math.BigDecimal laborHours;
    private String partsUsed;
    private String resolutionEvidenceUrl;
    private Instant resolvedAt;
    private UUID verifiedById;
    private String verifiedByName;
    private Instant verifiedAt;
    private String verificationNotes;
    private Instant slaDueAt;
    private boolean slaBreached;
    private String slaStatus;
    private Long slaRemainingMinutes;
    private RecurrenceIntelligenceResponse recurrenceIntelligence;
    private List<ExpenditureResponse> expenditures = new ArrayList<>();
    private List<AttachmentResponse> attachments = new ArrayList<>();
    private List<TimelineEventResponse> timeline = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;

    public IssueResponse() {
    }

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

    public UUID getOrgId() {
        return orgId;
    }

    public void setOrgId(UUID orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
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

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }

    public UUID getReporterId() {
        return reporterId;
    }

    public void setReporterId(UUID reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public UUID getAssignedTechnicianId() {
        return assignedTechnicianId;
    }

    public void setAssignedTechnicianId(UUID assignedTechnicianId) {
        this.assignedTechnicianId = assignedTechnicianId;
    }

    public String getAssignedTechnicianName() {
        return assignedTechnicianName;
    }

    public void setAssignedTechnicianName(String assignedTechnicianName) {
        this.assignedTechnicianName = assignedTechnicianName;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
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

    public String getBlockingReason() {
        return blockingReason;
    }

    public void setBlockingReason(String blockingReason) {
        this.blockingReason = blockingReason;
    }

    public ExplainableProgressResponse getExplainableProgress() {
        return explainableProgress;
    }

    public void setExplainableProgress(ExplainableProgressResponse explainableProgress) {
        this.explainableProgress = explainableProgress;
    }

    public List<AttachmentResponse> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentResponse> attachments) {
        this.attachments = attachments;
    }

    public List<TimelineEventResponse> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineEventResponse> timeline) {
        this.timeline = timeline;
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

    public UUID getVerifiedById() {
        return verifiedById;
    }

    public void setVerifiedById(UUID verifiedById) {
        this.verifiedById = verifiedById;
    }

    public String getVerifiedByName() {
        return verifiedByName;
    }

    public void setVerifiedByName(String verifiedByName) {
        this.verifiedByName = verifiedByName;
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

    public List<ExpenditureResponse> getExpenditures() {
        return expenditures;
    }

    public void setExpenditures(List<ExpenditureResponse> expenditures) {
        this.expenditures = expenditures;
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

    public String getSlaStatus() {
        return slaStatus;
    }

    public void setSlaStatus(String slaStatus) {
        this.slaStatus = slaStatus;
    }

    public Long getSlaRemainingMinutes() {
        return slaRemainingMinutes;
    }

    public void setSlaRemainingMinutes(Long slaRemainingMinutes) {
        this.slaRemainingMinutes = slaRemainingMinutes;
    }

    public RecurrenceIntelligenceResponse getRecurrenceIntelligence() {
        return recurrenceIntelligence;
    }

    public void setRecurrenceIntelligence(RecurrenceIntelligenceResponse recurrenceIntelligence) {
        this.recurrenceIntelligence = recurrenceIntelligence;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }
}
