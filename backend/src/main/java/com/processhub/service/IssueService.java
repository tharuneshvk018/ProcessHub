package com.processhub.service;

import com.processhub.domain.entity.*;
import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import com.processhub.domain.enums.Role;
import com.processhub.domain.enums.TimelineAction;
import com.processhub.domain.repository.*;
import com.processhub.dto.*;
import com.processhub.exception.DuplicateResourceException;
import com.processhub.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final IssueAttachmentRepository attachmentRepository;
    private final IssueTimelineEventRepository timelineRepository;
    private final DepartmentRepository departmentRepository;
    private final IssueCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationService organizationService;
    private final AttachmentService attachmentService;
    private final ExpenditureRequestRepository expenditureRequestRepository;
    private final SlaService slaService;
    private final RecurrenceIntelligenceService recurrenceIntelligenceService;
    private final NotificationService notificationService;
    private final OrganizationMembershipRepository membershipRepository;

    public IssueService(
            IssueRepository issueRepository,
            IssueAttachmentRepository attachmentRepository,
            IssueTimelineEventRepository timelineRepository,
            DepartmentRepository departmentRepository,
            IssueCategoryRepository categoryRepository,
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            OrganizationService organizationService,
            AttachmentService attachmentService,
            ExpenditureRequestRepository expenditureRequestRepository,
            SlaService slaService,
            RecurrenceIntelligenceService recurrenceIntelligenceService,
            NotificationService notificationService,
            OrganizationMembershipRepository membershipRepository) {
        this.issueRepository = issueRepository;
        this.attachmentRepository = attachmentRepository;
        this.timelineRepository = timelineRepository;
        this.departmentRepository = departmentRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.organizationService = organizationService;
        this.attachmentService = attachmentService;
        this.expenditureRequestRepository = expenditureRequestRepository;
        this.slaService = slaService;
        this.recurrenceIntelligenceService = recurrenceIntelligenceService;
        this.notificationService = notificationService;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public IssueResponse createIssue(UUID orgId, CreateIssueRequest request, String currentUserEmail) {
        // Enforce organization access isolation
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        if (!department.getOrganization().getId().equals(orgId)) {
            throw new IllegalArgumentException("Department " + request.getDepartmentId() + " does not belong to organization " + orgId);
        }

        if (!department.isActive()) {
            throw new IllegalArgumentException("Department " + department.getName() + " is inactive");
        }

        IssueCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        if (!category.getOrganization().getId().equals(orgId)) {
            throw new IllegalArgumentException("Category " + request.getCategoryId() + " does not belong to organization " + orgId);
        }

        if (!category.getDepartment().getId().equals(department.getId())) {
            throw new IllegalArgumentException("Category does not belong to department " + department.getName());
        }

        if (!category.isActive()) {
            throw new IllegalArgumentException("Category " + category.getName() + " is inactive");
        }

        // Validate asset requirement when category mandates an asset ID
        if (category.isRequiresAsset()) {
            if (request.getAssetId() == null || request.getAssetId().trim().isEmpty()) {
                throw new IllegalArgumentException("Asset identifier is mandatory for category: " + category.getName());
            }
        }

        // Prevent rapid duplicate submissions caused by repeated clicks (within 30 seconds)
        Instant thirtySecondsAgo = Instant.now().minus(30, ChronoUnit.SECONDS);
        boolean isDuplicate = issueRepository.existsByReporterIdAndOrganizationIdAndDepartmentIdAndCategoryIdAndTitleAndCreatedAtAfter(
                currentUser.getId(), orgId, department.getId(), category.getId(), request.getTitle().trim(), thirtySecondsAgo);

        if (isDuplicate) {
            throw new DuplicateResourceException(
                    "Duplicate submission detected: You recently submitted an identical problem report. Please wait before submitting again.");
        }

        // Generate unique atomic reference code (e.g. PH-1001)
        Long nextSequence = issueRepository.getNextIssueReferenceSequence();
        String referenceCode = String.format("PH-%04d", nextSequence);

        // Determine priority
        Priority priority = parsePriority(request.getPriority(), category.getDefaultPriority());

        Issue issue = new Issue(
                UUID.randomUUID(),
                referenceCode,
                organization,
                department,
                category,
                currentUser,
                request.getSubtype() != null ? request.getSubtype().trim() : null,
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getLocationBuilding().trim(),
                request.getLocationRoom().trim(),
                request.getAssetId() != null ? request.getAssetId().trim() : null,
                priority
        );

        Issue saved = issueRepository.save(issue);

        // Record immutable audit timeline event for issue submission
        IssueTimelineEvent timelineEvent = new IssueTimelineEvent(
                UUID.randomUUID(),
                saved,
                currentUser,
                TimelineAction.ISSUE_SUBMITTED.name(),
                null,
                IssueStatus.SUBMITTED.name(),
                "Problem report submitted: " + saved.getTitle()
        );
        timelineRepository.save(timelineEvent);

        // Notify active supervisors of the department
        List<OrganizationMembership> supervisors = membershipRepository
                .findByOrganizationIdAndDepartmentIdAndRoleAndActiveTrue(orgId, department.getId(), Role.ROLE_SUPERVISOR);
        for (OrganizationMembership sup : supervisors) {
            notificationService.createNotification(
                    orgId,
                    sup.getUser(),
                    saved,
                    com.processhub.domain.enums.NotificationType.ISSUE_SUBMITTED,
                    "New Issue: " + saved.getReferenceCode(),
                    "New problem report submitted in " + department.getName() + ": " + saved.getTitle()
            );
        }

        // Notify reporter with submission confirmation
        notificationService.createNotification(
                orgId,
                currentUser,
                saved,
                com.processhub.domain.enums.NotificationType.ISSUE_SUBMITTED,
                "Submission Confirmation: " + saved.getReferenceCode(),
                "Your problem report '" + saved.getTitle() + "' has been submitted successfully."
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public IssueResponse getIssue(UUID orgId, UUID issueId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        return mapToResponse(issue);
    }

    @Transactional(readOnly = true)
    public IssueResponse getIssueByReference(UUID orgId, String referenceCode, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        Issue issue = issueRepository.findByReferenceCodeAndOrganizationId(referenceCode.trim().toUpperCase(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with reference code: " + referenceCode));

        return mapToResponse(issue);
    }

    @Transactional(readOnly = true)
    public IssueResponse getIssueByReferenceDirect(String referenceCode, String currentUserEmail) {
        Issue issue = issueRepository.findByReferenceCode(referenceCode.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with reference code: " + referenceCode));

        // Enforce organization data isolation
        organizationService.validateUserOrganizationAccess(issue.getOrganization().getId(), currentUserEmail);

        return mapToResponse(issue);
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> getMyReports(UUID orgId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        List<Issue> issues = issueRepository.findByOrganizationIdAndReporterIdOrderByCreatedAtDesc(orgId, currentUser.getId());
        return issues.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReporterDashboardResponse getReporterDashboard(UUID orgId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        long totalReported = issueRepository.countByOrganizationIdAndReporterId(orgId, currentUser.getId());
        long resolvedIssues = issueRepository.countByOrganizationIdAndReporterIdAndStatus(orgId, currentUser.getId(), IssueStatus.CLOSED);
        long activeIssues = issueRepository.countByOrganizationIdAndReporterIdAndStatusNotIn(
                orgId, currentUser.getId(), List.of(IssueStatus.CLOSED, IssueStatus.REJECTED));
        long actionRequiredCount = issueRepository.countByOrganizationIdAndReporterIdAndStatus(
                orgId, currentUser.getId(), IssueStatus.MORE_INFO_REQUESTED);

        List<Issue> userIssues = issueRepository.findByOrganizationIdAndReporterIdOrderByCreatedAtDesc(orgId, currentUser.getId());
        List<IssueResponse> recentReports = userIssues.stream()
                .limit(10)
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new ReporterDashboardResponse(
                totalReported,
                activeIssues,
                resolvedIssues,
                actionRequiredCount,
                recentReports
        );
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> getOrganizationIssues(UUID orgId, IssueStatus status, UUID departmentId,
                                                      Priority priority, String search, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        List<Issue> issues = issueRepository.findByOrganizationIdOrderByCreatedAtDesc(orgId);

        return issues.stream()
                .filter(issue -> status == null || issue.getStatus() == status)
                .filter(issue -> departmentId == null || issue.getDepartment().getId().equals(departmentId))
                .filter(issue -> priority == null || issue.getPriority() == priority)
                .filter(issue -> {
                    if (search == null || search.trim().isEmpty()) return true;
                    String q = search.toLowerCase(Locale.ROOT).trim();
                    return issue.getReferenceCode().toLowerCase(Locale.ROOT).contains(q)
                            || issue.getTitle().toLowerCase(Locale.ROOT).contains(q)
                            || issue.getDescription().toLowerCase(Locale.ROOT).contains(q);
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Priority parsePriority(String requestedPriority, String defaultPriority) {
        if (requestedPriority != null && !requestedPriority.trim().isEmpty()) {
            String clean = requestedPriority.trim().toUpperCase(Locale.ROOT);
            if (clean.equals("CRITICAL_SAFETY")) {
                return Priority.CRITICAL;
            }
            try {
                return Priority.valueOf(clean);
            } catch (IllegalArgumentException ignored) {
                // fallback to default
            }
        }

        if (defaultPriority != null && !defaultPriority.trim().isEmpty()) {
            try {
                return Priority.valueOf(defaultPriority.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                // fallback to MEDIUM
            }
        }

        return Priority.MEDIUM;
    }

    public IssueResponse mapToResponse(Issue issue) {
        IssueResponse res = new IssueResponse();
        res.setId(issue.getId());
        res.setReferenceCode(issue.getReferenceCode());
        res.setOrgId(issue.getOrganization().getId());
        res.setOrgName(issue.getOrganization().getName());
        res.setDepartmentId(issue.getDepartment().getId());
        res.setDepartmentName(issue.getDepartment().getName());
        res.setCategoryId(issue.getCategory().getId());
        res.setCategoryName(issue.getCategory().getName());
        res.setCategoryCode(issue.getCategory().getCode());
        res.setSubtype(issue.getSubtype());
        res.setTitle(issue.getTitle());
        res.setDescription(issue.getDescription());
        res.setLocationBuilding(issue.getLocationBuilding());
        res.setLocationRoom(issue.getLocationRoom());
        res.setAssetId(issue.getAssetId());
        res.setPriority(issue.getPriority().name());
        res.setStatus(issue.getStatus().name());
        res.setStatusDisplayName(issue.getStatus().getDisplayName());

        res.setReporterId(issue.getReporter().getId());
        res.setReporterName(issue.getReporter().getFullName());
        res.setReporterEmail(issue.getReporter().getEmail());

        if (issue.getAssignedTechnician() != null) {
            res.setAssignedTechnicianId(issue.getAssignedTechnician().getId());
            res.setAssignedTechnicianName(issue.getAssignedTechnician().getFullName());
        }

        res.setCurrentStage(issue.getCurrentStage());
        res.setResponsibleRole(issue.getResponsibleRole());
        res.setNextAction(issue.getNextAction());
        res.setBlockingReason(issue.getBlockingReason());

        boolean isBlocked = issue.getBlockingReason() != null && !issue.getBlockingReason().trim().isEmpty();
        ExplainableProgressResponse explainable = new ExplainableProgressResponse(
                issue.getCurrentStage(),
                issue.getStatus().getDisplayName(),
                issue.getBlockingReason(),
                issue.getResponsibleRole(),
                issue.getNextAction(),
                isBlocked
        );
        res.setExplainableProgress(explainable);

        // Fetch attachments
        List<IssueAttachment> attachments = attachmentRepository.findByIssueIdOrderByCreatedAtAsc(issue.getId());
        List<AttachmentResponse> attachmentResponses = attachments.stream()
                .map(attachmentService::mapToResponse)
                .collect(Collectors.toList());
        res.setAttachments(attachmentResponses);

        // Fetch timeline
        List<IssueTimelineEvent> timelineEvents = timelineRepository.findByIssueIdOrderByCreatedAtAsc(issue.getId());
        List<TimelineEventResponse> timelineResponses = timelineEvents.stream()
                .map(event -> new TimelineEventResponse(
                        event.getId(),
                        event.getActor() != null ? event.getActor().getId() : null,
                        event.getActor() != null ? event.getActor().getFullName() : "System",
                        event.getAction(),
                        event.getFromStatus(),
                        event.getToStatus(),
                        event.getNotes(),
                        event.getCreatedAt()
                ))
                .collect(Collectors.toList());
        res.setTimeline(timelineResponses);

        res.setWorkSummary(issue.getWorkSummary());
        res.setRootCause(issue.getRootCause());
        res.setLaborHours(issue.getLaborHours());
        res.setPartsUsed(issue.getPartsUsed());
        res.setResolutionEvidenceUrl(issue.getResolutionEvidenceUrl());
        res.setResolvedAt(issue.getResolvedAt());
        if (issue.getVerifiedBy() != null) {
            res.setVerifiedById(issue.getVerifiedBy().getId());
            res.setVerifiedByName(issue.getVerifiedBy().getFullName());
        }
        res.setVerifiedAt(issue.getVerifiedAt());
        res.setVerificationNotes(issue.getVerificationNotes());

        // Fetch expenditures
        List<ExpenditureRequest> expenditures = expenditureRequestRepository.findByIssueIdOrderByCreatedAtAsc(issue.getId());
        List<ExpenditureResponse> expenditureResponses = expenditures.stream()
                .map(exp -> new ExpenditureResponse(
                        exp.getId(),
                        issue.getId(),
                        issue.getReferenceCode(),
                        exp.getRequestedBy().getId(),
                        exp.getRequestedBy().getFullName(),
                        exp.getAmount(),
                        exp.getJustification(),
                        exp.getStatus().name(),
                        exp.getDecidedBy() != null ? exp.getDecidedBy().getId() : null,
                        exp.getDecidedBy() != null ? exp.getDecidedBy().getFullName() : null,
                        exp.getDecisionNotes(),
                        exp.getCreatedAt(),
                        exp.getDecidedAt()
                ))
                .collect(Collectors.toList());
        res.setExpenditures(expenditureResponses);

        // SLA Info
        SlaInfoResponse slaInfo = slaService.getSlaInfo(issue);
        res.setSlaDueAt(slaInfo.getSlaDueAt());
        res.setSlaBreached(slaInfo.isSlaBreached());
        res.setSlaStatus(slaInfo.getSlaStatus());
        res.setSlaRemainingMinutes(slaInfo.getTimeRemainingMinutes());

        // Recurrence Intelligence
        RecurrenceIntelligenceResponse recurrence = recurrenceIntelligenceService.analyzeRecurrence(
                issue.getOrganization().getId(), issue);
        res.setRecurrenceIntelligence(recurrence);

        res.setCreatedAt(issue.getCreatedAt());
        res.setUpdatedAt(issue.getUpdatedAt());
        res.setClosedAt(issue.getClosedAt());

        return res;
    }
}
