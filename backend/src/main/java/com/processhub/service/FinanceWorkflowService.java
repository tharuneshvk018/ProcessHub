package com.processhub.service;

import com.processhub.domain.entity.*;
import com.processhub.domain.enums.ExpenditureStatus;
import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.NotificationType;
import com.processhub.domain.enums.Role;
import com.processhub.domain.enums.TimelineAction;
import com.processhub.domain.repository.*;
import com.processhub.dto.*;
import com.processhub.exception.InvalidWorkflowTransitionException;
import com.processhub.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FinanceWorkflowService {

    private final IssueRepository issueRepository;
    private final ExpenditureRequestRepository expenditureRepository;
    private final IssueTimelineEventRepository timelineRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final OrganizationService organizationService;
    private final IssueService issueService;
    private final NotificationService notificationService;

    @Value("${processhub.finance.auto-approval-threshold:100.00}")
    private BigDecimal autoApprovalThreshold = new BigDecimal("100.00");

    public FinanceWorkflowService(
            IssueRepository issueRepository,
            ExpenditureRequestRepository expenditureRepository,
            IssueTimelineEventRepository timelineRepository,
            OrganizationMembershipRepository membershipRepository,
            UserRepository userRepository,
            OrganizationService organizationService,
            IssueService issueService,
            NotificationService notificationService) {
        this.issueRepository = issueRepository;
        this.expenditureRepository = expenditureRepository;
        this.timelineRepository = timelineRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.organizationService = organizationService;
        this.issueService = issueService;
        this.notificationService = notificationService;
    }

    @Transactional
    public IssueResponse requestExpenditure(UUID orgId, UUID issueId, CreateExpenditureRequest request, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        if (issue.getStatus() != IssueStatus.IN_PROGRESS && issue.getStatus() != IssueStatus.ASSIGNED) {
            throw new InvalidWorkflowTransitionException(
                    "Cannot request expenditure for issue in status: " + issue.getStatus() + ". Must be IN_PROGRESS or ASSIGNED.");
        }

        boolean isAutoApproved = request.getAmount().compareTo(autoApprovalThreshold) <= 0;

        ExpenditureRequest expenditure = new ExpenditureRequest();
        expenditure.setId(UUID.randomUUID());
        expenditure.setIssue(issue);
        expenditure.setRequestedBy(currentUser);
        expenditure.setAmount(request.getAmount());
        expenditure.setJustification(request.getJustification());
        expenditure.setCreatedAt(Instant.now());

        if (isAutoApproved) {
            // Auto-approved under threshold
            expenditure.setStatus(ExpenditureStatus.APPROVED);
            expenditure.setDecidedAt(Instant.now());
            expenditure.setDecisionNotes("Auto-approved below threshold limit ($" + autoApprovalThreshold + ")");
            expenditureRepository.save(expenditure);

            // Issue remains in IN_PROGRESS
            issue.setUpdatedAt(Instant.now());
            Issue saved = issueRepository.save(issue);

            IssueTimelineEvent event = new IssueTimelineEvent(
                    UUID.randomUUID(),
                    saved,
                    currentUser,
                    TimelineAction.EXPENDITURE_APPROVED.name(),
                    saved.getStatus().name(),
                    saved.getStatus().name(),
                    "Auto-approved expenditure of $" + request.getAmount() + " (within threshold limit): " + request.getJustification()
            );
            timelineRepository.save(event);

            return issueService.mapToResponse(saved);
        } else {
            // Threshold exceeded - requires finance approval
            expenditure.setStatus(ExpenditureStatus.PENDING);
            expenditureRepository.save(expenditure);

            IssueStatus previousStatus = issue.getStatus();
            issue.setStatus(IssueStatus.AWAITING_FINANCE);
            issue.setCurrentStage("AWAITING_FINANCE");
            issue.setResponsibleRole(Role.ROLE_FINANCE_APPROVER.name());
            issue.setBlockingReason("Awaiting finance authorization for expenditure of $" + request.getAmount() + ": " + request.getJustification());
            issue.setNextAction("Finance queue review and authorization");
            issue.setUpdatedAt(Instant.now());

            Issue saved = issueRepository.save(issue);

            IssueTimelineEvent event = new IssueTimelineEvent(
                    UUID.randomUUID(),
                    saved,
                    currentUser,
                    TimelineAction.EXPENDITURE_REQUESTED.name(),
                    previousStatus.name(),
                    IssueStatus.AWAITING_FINANCE.name(),
                    "Expenditure of $" + request.getAmount() + " exceeds auto-approval threshold ($" + autoApprovalThreshold + "). Submitted to Finance: " + request.getJustification()
            );
            timelineRepository.save(event);

            // Notify authorized finance approver(s)
            List<OrganizationMembership> financeApprovers = membershipRepository
                    .findByOrganizationIdAndRoleAndActiveTrue(orgId, Role.ROLE_FINANCE_APPROVER);
            for (OrganizationMembership fin : financeApprovers) {
                notificationService.createNotification(
                        orgId,
                        fin.getUser(),
                        saved,
                        NotificationType.EXPENDITURE_REQUESTED,
                        "Finance Authorization Required: " + saved.getReferenceCode(),
                        "Technician " + currentUser.getFullName() + " requested $" + request.getAmount() + " for: " + request.getJustification()
                );
            }

            return issueService.mapToResponse(saved);
        }
    }

    @Transactional(readOnly = true)
    public List<ExpenditureResponse> getFinanceQueue(UUID orgId, ExpenditureStatus status, String currentUserEmail) {
        validateFinanceAccess(orgId, currentUserEmail);

        List<ExpenditureRequest> expenditures;
        if (status != null) {
            expenditures = expenditureRepository.findByIssueOrganizationIdAndStatusOrderByCreatedAtDesc(orgId, status);
        } else {
            expenditures = expenditureRepository.findByIssueOrganizationIdOrderByCreatedAtDesc(orgId);
        }

        return expenditures.stream()
                .map(exp -> new ExpenditureResponse(
                        exp.getId(),
                        exp.getIssue().getId(),
                        exp.getIssue().getReferenceCode(),
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
    }

    @Transactional(readOnly = true)
    public List<ExpenditureResponse> getIssueExpenditures(UUID orgId, UUID issueId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);
        return expenditureRepository.findByIssueIdOrderByCreatedAtAsc(issueId).stream()
                .map(exp -> new ExpenditureResponse(
                        exp.getId(),
                        exp.getIssue().getId(),
                        exp.getIssue().getReferenceCode(),
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
    }

    @Transactional(readOnly = true)
    public FinanceMetricsResponse getFinanceMetrics(UUID orgId, String currentUserEmail) {
        validateFinanceAccess(orgId, currentUserEmail);

        List<ExpenditureRequest> pendingList = expenditureRepository.findByIssueOrganizationIdAndStatusOrderByCreatedAtDesc(
                orgId, ExpenditureStatus.PENDING);
        long pendingCount = pendingList.size();
        BigDecimal pendingTotal = pendingList.stream()
                .map(ExpenditureRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long approvedCount = expenditureRepository.countByIssueOrganizationIdAndStatus(orgId, ExpenditureStatus.APPROVED);
        long rejectedCount = expenditureRepository.countByIssueOrganizationIdAndStatus(orgId, ExpenditureStatus.REJECTED);

        return new FinanceMetricsResponse(
                pendingCount,
                pendingTotal,
                approvedCount,
                rejectedCount
        );
    }

    @Transactional
    public IssueResponse decideExpenditure(UUID orgId, UUID issueId, UUID expenditureId,
                                          DecideExpenditureRequest request, String currentUserEmail) {
        validateFinanceAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        ExpenditureRequest expenditure = expenditureRepository.findById(expenditureId)
                .orElseThrow(() -> new ResourceNotFoundException("Expenditure request not found: " + expenditureId));

        if (!expenditure.getIssue().getId().equals(issueId)) {
            throw new IllegalArgumentException("Expenditure request does not belong to specified issue");
        }

        if (expenditure.getStatus() != ExpenditureStatus.PENDING) {
            throw new InvalidWorkflowTransitionException(
                    "Expenditure request is already decided with status: " + expenditure.getStatus());
        }

        expenditure.setDecidedBy(currentUser);
        expenditure.setDecidedAt(Instant.now());
        expenditure.setDecisionNotes(request.getNotes());

        IssueStatus previousStatus = issue.getStatus();

        if (Boolean.TRUE.equals(request.getApproved())) {
            expenditure.setStatus(ExpenditureStatus.APPROVED);
            expenditureRepository.save(expenditure);

            // Unblock issue: resume IN_PROGRESS
            issue.setStatus(IssueStatus.IN_PROGRESS);
            issue.setCurrentStage("IN_PROGRESS");
            issue.setResponsibleRole(Role.ROLE_TECHNICIAN.name());
            issue.setBlockingReason(null);
            issue.setNextAction("Technician resume execution with authorized expenditure");
            issue.setUpdatedAt(Instant.now());

            Issue saved = issueRepository.save(issue);

            String notes = "Finance authorized expenditure of $" + expenditure.getAmount();
            if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
                notes += " — Notes: " + request.getNotes();
            }

            IssueTimelineEvent event = new IssueTimelineEvent(
                    UUID.randomUUID(),
                    saved,
                    currentUser,
                    TimelineAction.EXPENDITURE_APPROVED.name(),
                    previousStatus.name(),
                    IssueStatus.IN_PROGRESS.name(),
                    notes
            );
            timelineRepository.save(event);

            // Notify assigned technician
            if (saved.getAssignedTechnician() != null) {
                notificationService.createNotification(
                        orgId,
                        saved.getAssignedTechnician(),
                        saved,
                        NotificationType.EXPENDITURE_DECIDED,
                        "Expenditure Authorized: " + saved.getReferenceCode(),
                        "Finance approved expenditure of $" + expenditure.getAmount() + ". Repair work may resume."
                );
            }

            // Notify department supervisors
            List<OrganizationMembership> supervisors = membershipRepository
                    .findByOrganizationIdAndDepartmentIdAndRoleAndActiveTrue(orgId, saved.getDepartment().getId(), Role.ROLE_SUPERVISOR);
            for (OrganizationMembership sup : supervisors) {
                notificationService.createNotification(
                        orgId,
                        sup.getUser(),
                        saved,
                        NotificationType.EXPENDITURE_DECIDED,
                        "Expenditure Authorized: " + saved.getReferenceCode(),
                        "Finance authorized $" + expenditure.getAmount() + " for issue " + saved.getReferenceCode()
                );
            }

            // Notify reporter
            notificationService.createNotification(
                    orgId,
                    saved.getReporter(),
                    saved,
                    NotificationType.STATUS_CHANGED,
                    "Work Resumed: " + saved.getReferenceCode(),
                    "Expenditure was authorized; repair work has resumed on your issue."
            );

            return issueService.mapToResponse(saved);
        } else {
            expenditure.setStatus(ExpenditureStatus.REJECTED);
            expenditureRepository.save(expenditure);

            // Re-route back to supervisor review
            issue.setStatus(IssueStatus.UNDER_REVIEW);
            issue.setCurrentStage("UNDER_REVIEW");
            issue.setResponsibleRole(Role.ROLE_SUPERVISOR.name());
            String blocking = "Expenditure request for $" + expenditure.getAmount() + " was rejected by Finance";
            if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
                blocking += ": " + request.getNotes();
            }
            issue.setBlockingReason(blocking);
            issue.setNextAction("Supervisor assess alternative resolution method or reassignment");
            issue.setUpdatedAt(Instant.now());

            Issue saved = issueRepository.save(issue);

            String eventNotes = "Finance rejected expenditure request for $" + expenditure.getAmount();
            if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
                eventNotes += ". Reason: " + request.getNotes();
            }

            IssueTimelineEvent event = new IssueTimelineEvent(
                    UUID.randomUUID(),
                    saved,
                    currentUser,
                    TimelineAction.EXPENDITURE_REJECTED.name(),
                    previousStatus.name(),
                    IssueStatus.UNDER_REVIEW.name(),
                    eventNotes
            );
            timelineRepository.save(event);

            // Notify assigned technician
            if (saved.getAssignedTechnician() != null) {
                notificationService.createNotification(
                        orgId,
                        saved.getAssignedTechnician(),
                        saved,
                        NotificationType.EXPENDITURE_DECIDED,
                        "Expenditure Declined: " + saved.getReferenceCode(),
                        "Finance declined expenditure of $" + expenditure.getAmount() + ": " + (request.getNotes() != null ? request.getNotes() : "Declined")
                );
            }

            // Notify department supervisors
            List<OrganizationMembership> supervisors = membershipRepository
                    .findByOrganizationIdAndDepartmentIdAndRoleAndActiveTrue(orgId, saved.getDepartment().getId(), Role.ROLE_SUPERVISOR);
            for (OrganizationMembership sup : supervisors) {
                notificationService.createNotification(
                        orgId,
                        sup.getUser(),
                        saved,
                        NotificationType.EXPENDITURE_DECIDED,
                        "Expenditure Declined: " + saved.getReferenceCode(),
                        "Finance declined expenditure for " + saved.getReferenceCode() + ". Supervisor reassessment required."
                );
            }

            // Notify reporter
            notificationService.createNotification(
                    orgId,
                    saved.getReporter(),
                    saved,
                    NotificationType.STATUS_CHANGED,
                    "Expenditure Declined: " + saved.getReferenceCode(),
                    "Expenditure for repair was declined by finance; supervisor is evaluating alternatives."
            );

            return issueService.mapToResponse(saved);
        }
    }

    private void validateFinanceAccess(UUID orgId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        boolean hasFinancePrivilege = membershipRepository.findByOrganizationIdAndUserId(orgId, currentUser.getId())
                .map(m -> m.getRole() == Role.ROLE_FINANCE_APPROVER || m.getRole() == Role.ROLE_ADMIN)
                .orElse(false);

        if (!hasFinancePrivilege) {
            throw new AccessDeniedException("User does not have Finance authorization privileges for this organization.");
        }
    }
}
