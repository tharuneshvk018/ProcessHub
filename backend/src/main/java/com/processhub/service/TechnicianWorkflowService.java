package com.processhub.service;

import com.processhub.domain.entity.*;
import com.processhub.domain.enums.ExpenditureStatus;
import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import com.processhub.domain.enums.Role;
import com.processhub.domain.enums.TimelineAction;
import com.processhub.domain.repository.*;
import com.processhub.dto.*;
import com.processhub.exception.InvalidWorkflowTransitionException;
import com.processhub.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TechnicianWorkflowService {

    private final IssueRepository issueRepository;
    private final IssueTimelineEventRepository timelineRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final ExpenditureRequestRepository expenditureRequestRepository;
    private final OrganizationService organizationService;
    private final IssueService issueService;

    public TechnicianWorkflowService(
            IssueRepository issueRepository,
            IssueTimelineEventRepository timelineRepository,
            OrganizationMembershipRepository membershipRepository,
            UserRepository userRepository,
            ExpenditureRequestRepository expenditureRequestRepository,
            OrganizationService organizationService,
            IssueService issueService) {
        this.issueRepository = issueRepository;
        this.timelineRepository = timelineRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.expenditureRequestRepository = expenditureRequestRepository;
        this.organizationService = organizationService;
        this.issueService = issueService;
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> getTechnicianWorkboard(UUID orgId, IssueStatus status, Priority priority, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        // Get issues assigned to this technician in this organization
        List<Issue> issues;
        if (status != null) {
            issues = issueRepository.findByOrganizationIdAndAssignedTechnicianIdAndStatusOrderByCreatedAtDesc(
                    orgId, currentUser.getId(), status);
        } else {
            issues = issueRepository.findByOrganizationIdAndAssignedTechnicianIdOrderByCreatedAtDesc(
                    orgId, currentUser.getId());
        }

        // Filter by priority if specified
        if (priority != null) {
            issues = issues.stream()
                    .filter(i -> i.getPriority() == priority)
                    .collect(Collectors.toList());
        }

        // Sort by priority descending (CRITICAL -> HIGH -> MEDIUM -> LOW), then createdAt desc
        Map<Priority, Integer> priorityRank = Map.of(
                Priority.CRITICAL, 4,
                Priority.HIGH, 3,
                Priority.MEDIUM, 2,
                Priority.LOW, 1
        );

        return issues.stream()
                .sorted((a, b) -> {
                    int pComp = Integer.compare(
                            priorityRank.getOrDefault(b.getPriority(), 0),
                            priorityRank.getOrDefault(a.getPriority(), 0));
                    if (pComp != 0) return pComp;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(issueService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TechnicianMetricsResponse getTechnicianMetrics(UUID orgId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        long assigned = issueRepository.countByOrganizationIdAndAssignedTechnicianIdAndStatus(
                orgId, currentUser.getId(), IssueStatus.ASSIGNED);
        long inProgress = issueRepository.countByOrganizationIdAndAssignedTechnicianIdAndStatus(
                orgId, currentUser.getId(), IssueStatus.IN_PROGRESS);
        long awaitingFinance = issueRepository.countByOrganizationIdAndAssignedTechnicianIdAndStatus(
                orgId, currentUser.getId(), IssueStatus.AWAITING_FINANCE);
        long pendingVerification = issueRepository.countByOrganizationIdAndAssignedTechnicianIdAndStatus(
                orgId, currentUser.getId(), IssueStatus.RESOLVED_PENDING_VERIFICATION);
        long completed = issueRepository.countByOrganizationIdAndAssignedTechnicianIdAndStatus(
                orgId, currentUser.getId(), IssueStatus.CLOSED);

        return new TechnicianMetricsResponse(
                assigned,
                inProgress,
                awaitingFinance,
                pendingVerification,
                completed
        );
    }

    @Transactional
    public IssueResponse startWork(UUID orgId, UUID issueId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        validateTechnicianAccess(issue, currentUser);

        if (issue.getStatus() != IssueStatus.ASSIGNED && issue.getStatus() != IssueStatus.REOPENED) {
            throw new InvalidWorkflowTransitionException(
                    "Cannot start work on issue in status: " + issue.getStatus() + ". Must be ASSIGNED or REOPENED.");
        }

        IssueStatus previousStatus = issue.getStatus();
        issue.setStatus(IssueStatus.IN_PROGRESS);
        issue.setCurrentStage("IN_PROGRESS");
        issue.setResponsibleRole(Role.ROLE_TECHNICIAN.name());
        issue.setBlockingReason(null);
        issue.setNextAction("Perform diagnostics, repair, or submit expenditure request if parts exceed threshold");
        issue.setUpdatedAt(Instant.now());

        Issue saved = issueRepository.save(issue);

        IssueTimelineEvent event = new IssueTimelineEvent(
                UUID.randomUUID(),
                saved,
                currentUser,
                TimelineAction.STATUS_CHANGED.name(),
                previousStatus.name(),
                IssueStatus.IN_PROGRESS.name(),
                "Work started by assigned technician: " + currentUser.getFullName()
        );
        timelineRepository.save(event);

        return issueService.mapToResponse(saved);
    }

    @Transactional
    public IssueResponse updateProgress(UUID orgId, UUID issueId, UpdateProgressRequest request, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        validateTechnicianAccess(issue, currentUser);

        if (issue.getStatus() != IssueStatus.IN_PROGRESS && issue.getStatus() != IssueStatus.AWAITING_FINANCE) {
            throw new InvalidWorkflowTransitionException(
                    "Cannot log progress on issue in status: " + issue.getStatus() + ". Must be IN_PROGRESS or AWAITING_FINANCE.");
        }

        issue.setUpdatedAt(Instant.now());
        Issue saved = issueRepository.save(issue);

        String notes = request.getNotes();
        if (request.getProgressPercentage() != null) {
            notes = "[" + request.getProgressPercentage() + "% Complete] " + notes;
        }

        IssueTimelineEvent event = new IssueTimelineEvent(
                UUID.randomUUID(),
                saved,
                currentUser,
                TimelineAction.PROGRESS_UPDATED.name(),
                saved.getStatus().name(),
                saved.getStatus().name(),
                notes
        );
        timelineRepository.save(event);

        return issueService.mapToResponse(saved);
    }

    @Transactional
    public IssueResponse submitResolution(UUID orgId, UUID issueId, SubmitResolutionRequest request, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        validateTechnicianAccess(issue, currentUser);

        if (issue.getStatus() != IssueStatus.IN_PROGRESS) {
            throw new InvalidWorkflowTransitionException(
                    "Cannot submit resolution for issue in status: " + issue.getStatus() + ". Must be IN_PROGRESS.");
        }

        // Check if there are pending expenditures
        List<ExpenditureRequest> expenditures = expenditureRequestRepository.findByIssueIdOrderByCreatedAtAsc(issue.getId());
        boolean hasPendingExpenditures = expenditures.stream()
                .anyMatch(exp -> exp.getStatus() == ExpenditureStatus.PENDING);
        if (hasPendingExpenditures) {
            throw new InvalidWorkflowTransitionException(
                    "Cannot resolve issue with pending expenditure requests. Awaiting finance decision first.");
        }

        IssueStatus previousStatus = issue.getStatus();
        issue.setStatus(IssueStatus.RESOLVED_PENDING_VERIFICATION);
        issue.setCurrentStage("RESOLVED_PENDING_VERIFICATION");
        issue.setResponsibleRole(Role.ROLE_SUPERVISOR.name());
        issue.setBlockingReason("Pending independent verification and sign-off");
        issue.setNextAction("Supervisor or Reporter quality and completion sign-off");

        issue.setWorkSummary(request.getWorkSummary());
        issue.setRootCause(request.getRootCause());
        issue.setLaborHours(request.getLaborHours());
        issue.setPartsUsed(request.getPartsUsed());
        issue.setResolutionEvidenceUrl(request.getResolutionEvidenceUrl());
        issue.setResolvedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());

        Issue saved = issueRepository.save(issue);

        // Record immutable timeline event
        String note = "Resolution submitted: " + request.getWorkSummary();
        if (request.getLaborHours() != null) {
            note += " (Labor: " + request.getLaborHours() + "h)";
        }
        IssueTimelineEvent event = new IssueTimelineEvent(
                UUID.randomUUID(),
                saved,
                currentUser,
                TimelineAction.SUBMITTED_FOR_VERIFICATION.name(),
                previousStatus.name(),
                IssueStatus.RESOLVED_PENDING_VERIFICATION.name(),
                note
        );
        timelineRepository.save(event);

        return issueService.mapToResponse(saved);
    }

    private void validateTechnicianAccess(Issue issue, User currentUser) {
        // Must be the assigned technician OR have technician/admin role in the department/org
        if (issue.getAssignedTechnician() != null && issue.getAssignedTechnician().getId().equals(currentUser.getId())) {
            return;
        }

        boolean hasPrivilege = membershipRepository.findByOrganizationIdAndUserId(issue.getOrganization().getId(), currentUser.getId())
                .map(m -> m.getRole() == Role.ROLE_TECHNICIAN || m.getRole() == Role.ROLE_SUPERVISOR || m.getRole() == Role.ROLE_ADMIN)
                .orElse(false);

        if (!hasPrivilege) {
            throw new AccessDeniedException("User is neither the assigned technician nor an authorized operational role.");
        }
    }
}
