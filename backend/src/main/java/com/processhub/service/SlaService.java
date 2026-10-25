package com.processhub.service;

import com.processhub.domain.entity.*;
import com.processhub.domain.enums.NotificationType;
import com.processhub.domain.enums.Role;
import com.processhub.domain.enums.SlaStatus;
import com.processhub.domain.enums.TimelineAction;
import com.processhub.domain.repository.IssueRepository;
import com.processhub.domain.repository.IssueTimelineEventRepository;
import com.processhub.domain.repository.OrganizationMembershipRepository;
import com.processhub.dto.SlaInfoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SlaService {

    private final IssueRepository issueRepository;
    private final IssueTimelineEventRepository timelineRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final NotificationService notificationService;

    public SlaService(
            IssueRepository issueRepository,
            IssueTimelineEventRepository timelineRepository,
            OrganizationMembershipRepository membershipRepository,
            NotificationService notificationService) {
        this.issueRepository = issueRepository;
        this.timelineRepository = timelineRepository;
        this.membershipRepository = membershipRepository;
        this.notificationService = notificationService;
    }

    public SlaInfoResponse getSlaInfo(Issue issue) {
        if (issue == null || issue.getSlaDueAt() == null) {
            return new SlaInfoResponse(null, false, SlaStatus.ON_TRACK.name(), 0);
        }

        Instant now = Instant.now();
        SlaStatus status = calculateStatus(issue, now);
        boolean breached = issue.isSlaBreached() || status == SlaStatus.BREACHED;

        long remainingMinutes = 0;
        if (now.isBefore(issue.getSlaDueAt())) {
            remainingMinutes = Duration.between(now, issue.getSlaDueAt()).toMinutes();
        }

        return new SlaInfoResponse(issue.getSlaDueAt(), breached, status.name(), remainingMinutes);
    }

    public SlaStatus calculateStatus(Issue issue, Instant now) {
        if (issue.getSlaDueAt() == null) {
            return SlaStatus.ON_TRACK;
        }

        if (issue.getClosedAt() != null) {
            return issue.getClosedAt().isAfter(issue.getSlaDueAt()) ? SlaStatus.BREACHED : SlaStatus.ON_TRACK;
        }

        if (now.isAfter(issue.getSlaDueAt()) || issue.isSlaBreached()) {
            return SlaStatus.BREACHED;
        }

        long totalMinutes = Duration.between(issue.getCreatedAt(), issue.getSlaDueAt()).toMinutes();
        long remainingMinutes = Duration.between(now, issue.getSlaDueAt()).toMinutes();

        if (totalMinutes > 0 && ((double) remainingMinutes / totalMinutes <= 0.25 || remainingMinutes <= 120)) {
            return SlaStatus.AT_RISK;
        }

        return SlaStatus.ON_TRACK;
    }

    @Transactional
    public int checkAndTriggerSlaEscalations(UUID orgId) {
        Instant now = Instant.now();
        List<Issue> overdueIssues = issueRepository
                .findByOrganizationIdAndClosedAtIsNullAndSlaDueAtBeforeAndSlaBreachedFalse(orgId, now);

        int escalatedCount = 0;

        for (Issue issue : overdueIssues) {
            issue.setSlaBreached(true);
            issue.setUpdatedAt(now);
            Issue saved = issueRepository.save(issue);
            escalatedCount++;

            // Record timeline event
            IssueTimelineEvent timelineEvent = new IssueTimelineEvent(
                    UUID.randomUUID(),
                    saved,
                    null,
                    TimelineAction.SLA_BREACHED.name(),
                    saved.getStatus().name(),
                    saved.getStatus().name(),
                    "Resolution SLA target breached. Target deadline was: " + issue.getSlaDueAt()
            );
            timelineRepository.save(timelineEvent);

            // Notify assigned technician if assigned
            if (issue.getAssignedTechnician() != null) {
                notificationService.createNotification(
                        orgId,
                        issue.getAssignedTechnician(),
                        saved,
                        NotificationType.SLA_BREACH_WARNING,
                        "SLA Breached: " + issue.getReferenceCode(),
                        "Resolution target deadline has elapsed for issue: " + issue.getTitle()
                );
            }

            // Notify department supervisors
            List<OrganizationMembership> supervisors = membershipRepository
                    .findByOrganizationIdAndDepartmentIdAndRoleAndActiveTrue(
                            orgId, issue.getDepartment().getId(), Role.ROLE_SUPERVISOR);

            for (OrganizationMembership sup : supervisors) {
                notificationService.createNotification(
                        orgId,
                        sup.getUser(),
                        saved,
                        NotificationType.SLA_BREACH_WARNING,
                        "Escalation: SLA Breached for " + issue.getReferenceCode(),
                        "Department issue " + issue.getReferenceCode() + " has exceeded its resolution SLA deadline."
                );
            }
        }

        return escalatedCount;
    }
}
