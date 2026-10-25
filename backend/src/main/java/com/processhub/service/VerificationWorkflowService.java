package com.processhub.service;

import com.processhub.domain.entity.*;
import com.processhub.domain.enums.IssueStatus;
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
import java.util.UUID;

@Service
public class VerificationWorkflowService {

    private final IssueRepository issueRepository;
    private final IssueTimelineEventRepository timelineRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final OrganizationService organizationService;
    private final IssueService issueService;

    public VerificationWorkflowService(
            IssueRepository issueRepository,
            IssueTimelineEventRepository timelineRepository,
            OrganizationMembershipRepository membershipRepository,
            UserRepository userRepository,
            OrganizationService organizationService,
            IssueService issueService) {
        this.issueRepository = issueRepository;
        this.timelineRepository = timelineRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.organizationService = organizationService;
        this.issueService = issueService;
    }

    @Transactional
    public IssueResponse verifyResolution(UUID orgId, UUID issueId, VerifyResolutionRequest request, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Issue issue = issueRepository.findByIdAndOrganizationId(issueId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found: " + issueId));

        if (issue.getStatus() != IssueStatus.RESOLVED_PENDING_VERIFICATION) {
            throw new InvalidWorkflowTransitionException(
                    "Cannot verify issue in status: " + issue.getStatus() + ". Must be RESOLVED_PENDING_VERIFICATION.");
        }

        // ENFORCE SEPARATION OF DUTIES: Technician who worked on the resolution cannot verify own work
        if (issue.getAssignedTechnician() != null && issue.getAssignedTechnician().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Technician cannot independently verify their own work. Separation of duties requires supervisor, administrator, or reporter verification.");
        }

        // Must be reporter, supervisor, or admin
        boolean isReporter = issue.getReporter().getId().equals(currentUser.getId());
        boolean hasVerifierRole = membershipRepository.findByOrganizationIdAndUserId(orgId, currentUser.getId())
                .map(m -> m.getRole() == Role.ROLE_SUPERVISOR || m.getRole() == Role.ROLE_ADMIN)
                .orElse(false);

        if (!isReporter && !hasVerifierRole) {
            throw new AccessDeniedException("User is not authorized to verify this issue. Must be reporter, supervisor, or administrator.");
        }

        IssueStatus previousStatus = issue.getStatus();

        if (Boolean.TRUE.equals(request.getApproved())) {
            // Verification passed: close issue
            issue.setStatus(IssueStatus.CLOSED);
            issue.setCurrentStage("CLOSED");
            issue.setResponsibleRole("NONE");
            issue.setBlockingReason(null);
            issue.setNextAction("None (Resolution Complete)");
            issue.setClosedAt(Instant.now());
            issue.setVerifiedBy(currentUser);
            issue.setVerifiedAt(Instant.now());
            issue.setVerificationNotes(request.getVerificationNotes());
            issue.setUpdatedAt(Instant.now());

            Issue saved = issueRepository.save(issue);

            IssueTimelineEvent event = new IssueTimelineEvent(
                    UUID.randomUUID(),
                    saved,
                    currentUser,
                    TimelineAction.VERIFICATION_PASSED.name(),
                    previousStatus.name(),
                    IssueStatus.CLOSED.name(),
                    "Resolution verified and sign-off granted by " + currentUser.getFullName() + ": " + request.getVerificationNotes()
            );
            timelineRepository.save(event);

            return issueService.mapToResponse(saved);
        } else {
            // Verification failed: reopen / return to IN_PROGRESS for rework
            issue.setStatus(IssueStatus.IN_PROGRESS);
            issue.setCurrentStage("IN_PROGRESS");
            issue.setResponsibleRole(Role.ROLE_TECHNICIAN.name());
            issue.setBlockingReason("Verification rejected: " + request.getVerificationNotes());
            issue.setNextAction("Technician address verification failure notes and resubmit for verification");
            issue.setVerificationNotes(request.getVerificationNotes());
            issue.setUpdatedAt(Instant.now());

            Issue saved = issueRepository.save(issue);

            IssueTimelineEvent event = new IssueTimelineEvent(
                    UUID.randomUUID(),
                    saved,
                    currentUser,
                    TimelineAction.VERIFICATION_FAILED.name(),
                    previousStatus.name(),
                    IssueStatus.IN_PROGRESS.name(),
                    "Verification rejected by " + currentUser.getFullName() + ": " + request.getVerificationNotes()
            );
            timelineRepository.save(event);

            return issueService.mapToResponse(saved);
        }
    }
}
