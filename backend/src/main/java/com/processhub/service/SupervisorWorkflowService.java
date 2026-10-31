package com.processhub.service;

import com.processhub.domain.entity.*;
import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.NotificationType;
import com.processhub.domain.enums.Priority;
import com.processhub.domain.enums.Role;
import com.processhub.domain.enums.TimelineAction;
import com.processhub.domain.repository.*;
import com.processhub.dto.*;
import com.processhub.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SupervisorWorkflowService {

    private final IssueRepository issueRepository;
    private final IssueTimelineEventRepository timelineRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final OrganizationService organizationService;
    private final IssueService issueService;
    private final WorkflowRoutingService routingService;
    private final NotificationService notificationService;

    public SupervisorWorkflowService(
            IssueRepository issueRepository,
            IssueTimelineEventRepository timelineRepository,
            OrganizationMembershipRepository membershipRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            OrganizationService organizationService,
            IssueService issueService,
            WorkflowRoutingService routingService,
            NotificationService notificationService) {
        this.issueRepository = issueRepository;
        this.timelineRepository = timelineRepository;
        this.membershipRepository = membershipRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.organizationService = organizationService;
        this.issueService = issueService;
        this.routingService = routingService;
        this.notificationService = notificationService;
    }

    public User validateSupervisorOrAdmin(UUID orgId, String userEmail) {
        organizationService.validateUserOrganizationAccess(orgId, userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (user.getRole() == Role.ROLE_ADMIN) {
            return user;
        }

        boolean isOrgAdmin = membershipRepository.existsByOrganizationIdAndUserIdAndRoleAndActiveTrue(
                orgId, user.getId(), Role.ROLE_ADMIN);
        if (isOrgAdmin) {
            return user;
        }

        boolean isSupervisor = membershipRepository.existsByOrganizationIdAndUserIdAndRoleAndActiveTrue(
                orgId, user.getId(), Role.ROLE_SUPERVISOR);
        if (!isSupervisor) {
            throw new AccessDeniedException("Access denied: Supervisor or administrative privileges required for this action");
        }

        return user;
    }

    public void validateSupervisorForDepartment(UUID orgId, UUID departmentId, String userEmail) {
        User user = validateSupervisorOrAdmin(orgId, userEmail);

        if (user.getRole() == Role.ROLE_ADMIN) {
            return;
        }

        boolean isOrgAdmin = membershipRepository.existsByOrganizationIdAndUserIdAndRoleAndActiveTrue(
                orgId, user.getId(), Role.ROLE_ADMIN);
        if (isOrgAdmin) {
            return;
        }

        boolean isDeptSupervisor = membershipRepository.existsByOrganizationIdAndDepartmentIdAndUserIdAndRoleAndActiveTrue(
                orgId, departmentId, user.getId(), Role.ROLE_SUPERVISOR);

        if (!isDeptSupervisor) {
            throw new AccessDeniedException("Access denied: You are not authorized as supervisor for department " + departmentId);
        }
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> getSupervisorQueue(
            UUID orgId, UUID departmentId, IssueStatus status, Priority priority, String userEmail) {
        User user = validateSupervisorOrAdmin(orgId, userEmail);

        boolean isOrgAdmin = user.getRole() == Role.ROLE_ADMIN ||
                membershipRepository.existsByOrganizationIdAndUserIdAndRoleAndActiveTrue(orgId, user.getId(), Role.ROLE_ADMIN);

        List<UUID> targetDeptIds;
        if (isOrgAdmin) {
            if (departmentId != null) {
                targetDeptIds = List.of(departmentId);
            } else {
                targetDeptIds = departmentRepository.findByOrganizationIdAndActiveTrue(orgId).stream()
                        .map(Department::getId)
                        .collect(Collectors.toList());
            }
        } else {
            List<OrganizationMembership> supervisorMemberships = membershipRepository
                    .findByOrganizationIdAndUserIdAndRoleAndActiveTrue(orgId, user.getId(), Role.ROLE_SUPERVISOR);

            Set<UUID> supervisedDeptIds = supervisorMemberships.stream()
                    .filter(m -> m.getDepartment() != null)
                    .map(m -> m.getDepartment().getId())
                    .collect(Collectors.toSet());

            if (departmentId != null) {
                if (!supervisedDeptIds.contains(departmentId)) {
                    throw new AccessDeniedException("Access denied: You do not supervise department " + departmentId);
                }
                targetDeptIds = List.of(departmentId);
            } else {
                targetDeptIds = new ArrayList<>(supervisedDeptIds);
            }
        }

        if (targetDeptIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Issue> issues = issueRepository.findByOrganizationIdAndDepartmentIdInOrderByCreatedAtDesc(orgId, targetDeptIds);

        return issues.stream()
                .filter(issue -> status == null || issue.getStatus() == status)
                .filter(issue -> priority == null || issue.getPriority() == priority)
                .map(issueService::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupervisorMetricsResponse getSupervisorMetrics(UUID orgId, String userEmail) {
        List<IssueResponse> queue = getSupervisorQueue(orgId, null, null, null, userEmail);

        long pendingReview = queue.stream()
                .filter(i -> "SUBMITTED".equals(i.getStatus()) || "UNDER_REVIEW".equals(i.getStatus()))
                .count();

        long unassigned = queue.stream()
                .filter(i -> i.getAssignedTechnicianId() == null && !"CLOSED".equals(i.getStatus()) && !"REJECTED".equals(i.getStatus()))
                .count();

        long awaitingRfi = queue.stream()
                .filter(i -> "MORE_INFO_REQUESTED".equals(i.getStatus()))
                .count();

        long criticalUrgent = queue.stream()
                .filter(i -> "CRITICAL".equals(i.getPriority()) && !"CLOSED".equals(i.getStatus()) && !"REJECTED".equals(i.getStatus()))
                .count();

        long routingExceptions = queue.stream()
                .filter(i -> "UNASSIGNED_TRIAGE".equals(i.getCurrentStage()))
                .count();

        long totalSupervised = queue.stream()
                .filter(i -> !"CLOSED".equals(i.getStatus()) && !"REJECTED".equals(i.getStatus()))
                .count();

        return new SupervisorMetricsResponse(
                pendingReview,
                unassigned,
                awaitingRfi,
                criticalUrgent,
                routingExceptions,
                totalSupervised
        );
    }

    @Transactional(readOnly = true)
    public List<TechnicianSummaryResponse> getDepartmentTechnicians(UUID orgId, UUID departmentId, String userEmail) {
        validateSupervisorForDepartment(orgId, departmentId, userEmail);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));

        List<OrganizationMembership> techMemberships = membershipRepository
                .findByOrganizationIdAndDepartmentIdAndRoleAndActiveTrue(orgId, departmentId, Role.ROLE_TECHNICIAN);

        return techMemberships.stream()
                .map(m -> {
                    User tech = m.getUser();
                    long activeCount = issueRepository.countByAssignedTechnicianIdAndStatusNotIn(
                            tech.getId(), List.of(IssueStatus.CLOSED, IssueStatus.REJECTED));
                    return new TechnicianSummaryResponse(
                            tech.getId(),
                            tech.getFullName(),
                            tech.getEmail(),
                            tech.getPhoneNumber(),
                            department.getId(),
                            department.getName(),
                            activeCount
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueResponse evaluateAndApplyRouting(UUID orgId, UUID issueId, String userEmail) {
        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        validateSupervisorOrAdmin(orgId, userEmail);

        RoutingDecision decision = routingService.evaluateRouting(issue);

        issue.setCurrentStage(decision.getStage());
        issue.setResponsibleRole(decision.getResponsibleRole());
        issue.setBlockingReason(decision.getBlockingReason());
        issue.setNextAction(decision.getNextAction());

        if (decision.isRoutingException()) {
            IssueTimelineEvent timelineEvent = new IssueTimelineEvent(
                    UUID.randomUUID(),
                    issue,
                    null,
                    TimelineAction.ROUTING_EXCEPTION.name(),
                    issue.getStatus().name(),
                    issue.getStatus().name(),
                    decision.getBlockingReason()
            );
            timelineRepository.save(timelineEvent);
        }

        Issue saved = issueRepository.save(issue);
        return issueService.mapToResponse(saved);
    }

    @Transactional
    public IssueResponse startReview(UUID orgId, UUID issueId, String userEmail) {
        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        validateSupervisorForDepartment(orgId, issue.getDepartment().getId(), userEmail);
        User supervisor = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (issue.getStatus() == IssueStatus.CLOSED || issue.getStatus() == IssueStatus.REJECTED) {
            throw new IllegalArgumentException("Cannot start review on a completed or rejected issue");
        }

        IssueStatus previousStatus = issue.getStatus();
        issue.setStatus(IssueStatus.UNDER_REVIEW);
        issue.setCurrentStage("UNDER_REVIEW");
        issue.setResponsibleRole(Role.ROLE_SUPERVISOR.name());
        issue.setBlockingReason(null);
        issue.setNextAction("Supervisor (" + supervisor.getFullName() + ") is reviewing report and preparing assignment or RFI");

        Issue saved = issueRepository.save(issue);

        IssueTimelineEvent timelineEvent = new IssueTimelineEvent(
                UUID.randomUUID(),
                saved,
                supervisor,
                TimelineAction.REVIEW_STARTED.name(),
                previousStatus.name(),
                IssueStatus.UNDER_REVIEW.name(),
                "Supervisor review initiated by " + supervisor.getFullName()
        );
        timelineRepository.save(timelineEvent);

        return issueService.mapToResponse(saved);
    }

    @Transactional
    public IssueResponse approveIssue(UUID orgId, UUID issueId, ApproveIssueRequest request, String userEmail) {
        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        validateSupervisorForDepartment(orgId, issue.getDepartment().getId(), userEmail);
        User supervisor = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (issue.getStatus() == IssueStatus.CLOSED || issue.getStatus() == IssueStatus.REJECTED) {
            throw new IllegalArgumentException("Cannot approve an issue that is already closed or rejected");
        }

        IssueStatus previousStatus = issue.getStatus();

        // Optional priority override by supervisor during triage
        if (request.getPriorityOverride() != null && !request.getPriorityOverride().isBlank()) {
            try {
                issue.setPriority(Priority.valueOf(request.getPriorityOverride().trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid priority override: " + request.getPriorityOverride());
            }
        }

        // Direct assignment to technician if technicianId provided
        if (request.getTechnicianId() != null) {
            User technician = userRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + request.getTechnicianId()));

            boolean isTech = membershipRepository.existsByOrganizationIdAndUserIdAndRoleAndActiveTrue(
                    orgId, technician.getId(), Role.ROLE_TECHNICIAN);
            if (!isTech) {
                throw new IllegalArgumentException("User " + technician.getFullName() + " is not an active technician in this organization");
            }

            issue.setAssignedTechnician(technician);
            issue.setStatus(IssueStatus.ASSIGNED);
            issue.setCurrentStage("ASSIGNED");
            issue.setResponsibleRole(Role.ROLE_TECHNICIAN.name());
            issue.setBlockingReason(null);
            issue.setNextAction("Assigned technician (" + technician.getFullName() + ") must commence diagnosis and repair");

            Issue saved = issueRepository.save(issue);

            String note = "Report approved and assigned to technician " + technician.getFullName()
                    + (request.getNotes() != null && !request.getNotes().isBlank() ? ". Notes: " + request.getNotes().trim() : "");

            IssueTimelineEvent timelineEvent = new IssueTimelineEvent(
                    UUID.randomUUID(),
                    saved,
                    supervisor,
                    TimelineAction.TECHNICIAN_ASSIGNED.name(),
                    previousStatus.name(),
                    IssueStatus.ASSIGNED.name(),
                    note
            );
            timelineRepository.save(timelineEvent);

            // Notify technician
            notificationService.createNotification(
                    orgId,
                    technician,
                    saved,
                    NotificationType.ISSUE_ASSIGNED,
                    "Work Order Assigned: " + saved.getReferenceCode(),
                    "You have been assigned to: " + saved.getTitle()
            );

            // Notify reporter
            notificationService.createNotification(
                    orgId,
                    saved.getReporter(),
                    saved,
                    NotificationType.STATUS_CHANGED,
                    "Report Approved & Assigned: " + saved.getReferenceCode(),
                    "Your report has been approved and assigned to technician " + technician.getFullName()
            );

            return issueService.mapToResponse(saved);
        } else {
            issue.setStatus(IssueStatus.UNDER_REVIEW);
            issue.setCurrentStage("APPROVED_AWAITING_ASSIGNMENT");
            issue.setResponsibleRole(Role.ROLE_SUPERVISOR.name());
            issue.setBlockingReason(null);
            issue.setNextAction("Issue approved; supervisor must assign an eligible technician");

            Issue saved = issueRepository.save(issue);

            String note = "Report approved by supervisor " + supervisor.getFullName()
                    + (request.getNotes() != null && !request.getNotes().isBlank() ? ". Notes: " + request.getNotes().trim() : "");

            IssueTimelineEvent timelineEvent = new IssueTimelineEvent(
                    UUID.randomUUID(),
                    saved,
                    supervisor,
                    TimelineAction.ISSUE_APPROVED.name(),
                    previousStatus.name(),
                    IssueStatus.UNDER_REVIEW.name(),
                    note
            );
            timelineRepository.save(timelineEvent);

            // Notify reporter
            notificationService.createNotification(
                    orgId,
                    saved.getReporter(),
                    saved,
                    NotificationType.STATUS_CHANGED,
                    "Report Approved: " + saved.getReferenceCode(),
                    "Your report has been approved by supervisor."
            );

            return issueService.mapToResponse(saved);
        }
    }

    @Transactional
    public IssueResponse rejectIssue(UUID orgId, UUID issueId, RejectIssueRequest request, String userEmail) {
        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        validateSupervisorForDepartment(orgId, issue.getDepartment().getId(), userEmail);
        User supervisor = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (issue.getStatus() == IssueStatus.CLOSED || issue.getStatus() == IssueStatus.REJECTED) {
            throw new IllegalArgumentException("Cannot reject an issue that is already closed or rejected");
        }

        String reason = request.getReason().trim();
        IssueStatus previousStatus = issue.getStatus();

        issue.setStatus(IssueStatus.REJECTED);
        issue.setCurrentStage("REJECTED");
        issue.setResponsibleRole(Role.ROLE_REPORTER.name());
        issue.setBlockingReason("Report rejected by supervisor: " + reason);
        issue.setNextAction("No further operational action required. Case is closed.");
        issue.setClosedAt(Instant.now());

        Issue saved = issueRepository.save(issue);

        IssueTimelineEvent timelineEvent = new IssueTimelineEvent(
                UUID.randomUUID(),
                saved,
                supervisor,
                TimelineAction.ISSUE_REJECTED.name(),
                previousStatus.name(),
                IssueStatus.REJECTED.name(),
                "Report rejected. Mandatory reason: " + reason
        );
        timelineRepository.save(timelineEvent);

        // Notify reporter
        notificationService.createNotification(
                orgId,
                saved.getReporter(),
                saved,
                NotificationType.STATUS_CHANGED,
                "Report Rejected: " + saved.getReferenceCode(),
                "Your report was rejected with reason: " + reason
        );

        return issueService.mapToResponse(saved);
    }

    @Transactional
    public IssueResponse requestInformation(UUID orgId, UUID issueId, RfiRequest request, String userEmail) {
        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        validateSupervisorForDepartment(orgId, issue.getDepartment().getId(), userEmail);
        User supervisor = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (issue.getStatus() == IssueStatus.CLOSED || issue.getStatus() == IssueStatus.REJECTED) {
            throw new IllegalArgumentException("Cannot request information for a closed or rejected issue");
        }

        String query = request.getQuestion().trim();
        IssueStatus previousStatus = issue.getStatus();

        issue.setStatus(IssueStatus.MORE_INFO_REQUESTED);
        issue.setCurrentStage("MORE_INFO_REQUESTED");
        issue.setResponsibleRole(Role.ROLE_REPORTER.name());
        issue.setBlockingReason("Supervisor requested additional information: " + query);
        issue.setNextAction("Reporter (" + issue.getReporter().getFullName() + ") must respond with requested details to unblock triage");

        Issue saved = issueRepository.save(issue);

        IssueTimelineEvent timelineEvent = new IssueTimelineEvent(
                UUID.randomUUID(),
                saved,
                supervisor,
                TimelineAction.MORE_INFO_REQUESTED.name(),
                previousStatus.name(),
                IssueStatus.MORE_INFO_REQUESTED.name(),
                "Request for Information: " + query
        );
        timelineRepository.save(timelineEvent);

        // Notify reporter
        notificationService.createNotification(
                orgId,
                saved.getReporter(),
                saved,
                NotificationType.RFI_REQUESTED,
                "Information Requested: " + saved.getReferenceCode(),
                "Supervisor requested clarification: " + query
        );

        return issueService.mapToResponse(saved);
    }

    @Transactional
    public IssueResponse provideInformation(UUID orgId, UUID issueId, ProvideInfoRequest request, String userEmail) {
        organizationService.validateUserOrganizationAccess(orgId, userEmail);

        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        // Enforce that caller is the reporter or org admin
        boolean isReporter = issue.getReporter().getId().equals(currentUser.getId());
        boolean isOrgAdmin = currentUser.getRole() == Role.ROLE_ADMIN ||
                membershipRepository.existsByOrganizationIdAndUserIdAndRoleAndActiveTrue(orgId, currentUser.getId(), Role.ROLE_ADMIN);

        if (!isReporter && !isOrgAdmin) {
            throw new AccessDeniedException("Access denied: Only the original reporter or an organization administrator can respond to an RFI");
        }

        if (issue.getStatus() != IssueStatus.MORE_INFO_REQUESTED) {
            throw new IllegalArgumentException("Issue is not currently awaiting additional information (current status: " + issue.getStatus() + ")");
        }

        String responseText = request.getResponse().trim();
        IssueStatus previousStatus = issue.getStatus();

        // Append provided information to description
        String updatedDescription = issue.getDescription()
                + "\n\n--- [Additional Information Provided by " + currentUser.getFullName() + " at " + Instant.now() + "] ---\n"
                + responseText;
        issue.setDescription(updatedDescription);

        // Transition back to UNDER_REVIEW
        issue.setStatus(IssueStatus.UNDER_REVIEW);
        issue.setCurrentStage("UNDER_REVIEW");
        issue.setResponsibleRole(Role.ROLE_SUPERVISOR.name());
        issue.setBlockingReason(null);
        issue.setNextAction("Supervisor must review provided information and proceed with triage and assignment");

        Issue saved = issueRepository.save(issue);

        IssueTimelineEvent timelineEvent = new IssueTimelineEvent(
                UUID.randomUUID(),
                saved,
                currentUser,
                TimelineAction.MORE_INFO_PROVIDED.name(),
                previousStatus.name(),
                IssueStatus.UNDER_REVIEW.name(),
                "Additional information provided: " + responseText
        );
        timelineRepository.save(timelineEvent);

        // Notify department supervisors
        List<OrganizationMembership> supervisors = membershipRepository
                .findByOrganizationIdAndDepartmentIdAndRoleAndActiveTrue(orgId, saved.getDepartment().getId(), Role.ROLE_SUPERVISOR);
        for (OrganizationMembership sup : supervisors) {
            notificationService.createNotification(
                    orgId,
                    sup.getUser(),
                    saved,
                    NotificationType.RFI_PROVIDED,
                    "Information Provided: " + saved.getReferenceCode(),
                    "Reporter provided requested information for ticket " + saved.getReferenceCode()
            );
        }

        return issueService.mapToResponse(saved);
    }

    @Transactional
    public IssueResponse assignTechnician(UUID orgId, UUID issueId, AssignTechnicianRequest request, String userEmail) {
        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with ID: " + issueId));

        validateSupervisorForDepartment(orgId, issue.getDepartment().getId(), userEmail);
        User supervisor = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        if (issue.getStatus() == IssueStatus.CLOSED || issue.getStatus() == IssueStatus.REJECTED) {
            throw new IllegalArgumentException("Cannot assign technician to a closed or rejected issue");
        }

        User technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + request.getTechnicianId()));

        boolean isTech = membershipRepository.existsByOrganizationIdAndUserIdAndRoleAndActiveTrue(
                orgId, technician.getId(), Role.ROLE_TECHNICIAN);
        if (!isTech) {
            throw new IllegalArgumentException("User " + technician.getFullName() + " is not an active technician in this organization");
        }

        IssueStatus previousStatus = issue.getStatus();

        issue.setAssignedTechnician(technician);
        issue.setStatus(IssueStatus.ASSIGNED);
        issue.setCurrentStage("ASSIGNED");
        issue.setResponsibleRole(Role.ROLE_TECHNICIAN.name());
        issue.setBlockingReason(null);
        issue.setNextAction("Assigned technician (" + technician.getFullName() + ") must commence diagnosis and repair");

        Issue saved = issueRepository.save(issue);

        String note = "Technician " + technician.getFullName() + " assigned"
                + (request.getInstructions() != null && !request.getInstructions().isBlank() ? ". Instructions: " + request.getInstructions().trim() : "");

        IssueTimelineEvent timelineEvent = new IssueTimelineEvent(
                UUID.randomUUID(),
                saved,
                supervisor,
                TimelineAction.TECHNICIAN_ASSIGNED.name(),
                previousStatus.name(),
                IssueStatus.ASSIGNED.name(),
                note
        );
        timelineRepository.save(timelineEvent);

        // Notify technician
        notificationService.createNotification(
                orgId,
                technician,
                saved,
                NotificationType.ISSUE_ASSIGNED,
                "Work Order Assigned: " + saved.getReferenceCode(),
                "You have been assigned to: " + saved.getTitle()
        );

        // Notify reporter
        notificationService.createNotification(
                orgId,
                saved.getReporter(),
                saved,
                NotificationType.STATUS_CHANGED,
                "Technician Assigned: " + saved.getReferenceCode(),
                "Technician " + technician.getFullName() + " has been assigned to your issue."
        );

        return issueService.mapToResponse(saved);
    }
}
