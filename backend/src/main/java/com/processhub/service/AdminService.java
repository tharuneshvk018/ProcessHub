package com.processhub.service;

import com.processhub.domain.entity.*;
import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.*;
import com.processhub.dto.*;
import com.processhub.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final DepartmentRepository departmentRepository;
    private final IssueRepository issueRepository;
    private final IssueTimelineEventRepository timelineRepository;
    private final OrganizationService organizationService;

    public AdminService(
            UserRepository userRepository,
            OrganizationMembershipRepository membershipRepository,
            DepartmentRepository departmentRepository,
            IssueRepository issueRepository,
            IssueTimelineEventRepository timelineRepository,
            OrganizationService organizationService) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.departmentRepository = departmentRepository;
        this.issueRepository = issueRepository;
        this.timelineRepository = timelineRepository;
        this.organizationService = organizationService;
    }

    public void validateAdminAccess(UUID orgId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        boolean isOrgAdmin = currentUser.getRole() == Role.ROLE_ADMIN ||
                membershipRepository.existsByOrganizationIdAndUserIdAndRoleAndActiveTrue(orgId, currentUser.getId(), Role.ROLE_ADMIN);

        if (!isOrgAdmin) {
            throw new AccessDeniedException("Access denied: Administrative privileges required.");
        }
    }

    @Transactional(readOnly = true)
    public List<AdminUserSummaryResponse> getOrganizationUsers(UUID orgId, String currentUserEmail) {
        validateAdminAccess(orgId, currentUserEmail);

        List<OrganizationMembership> memberships = membershipRepository.findByOrganizationIdAndActiveTrue(orgId);

        return memberships.stream().map(m -> {
            User u = m.getUser();
            String deptName = m.getDepartment() != null ? m.getDepartment().getName() : (u.getDepartment() != null ? u.getDepartment() : "Organization Wide");
            return new AdminUserSummaryResponse(
                    u.getId(),
                    u.getEmail(),
                    u.getFullName(),
                    u.getPhoneNumber(),
                    m.getRole().name(),
                    deptName,
                    u.isActive() && m.isActive(),
                    u.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminActivityResponse> getOrganizationActivities(UUID orgId, int limit, String currentUserEmail) {
        validateAdminAccess(orgId, currentUserEmail);

        List<IssueTimelineEvent> events = timelineRepository.findByIssueOrganizationIdOrderByCreatedAtDesc(orgId);

        return events.stream()
                .limit(limit > 0 ? limit : 50)
                .map(e -> {
                    User actor = e.getActor();
                    String actorRole = actor != null ? actor.getRole().name() : "SYSTEM";
                    return new AdminActivityResponse(
                            e.getId(),
                            e.getIssue().getId(),
                            e.getIssue().getReferenceCode(),
                            e.getIssue().getTitle(),
                            actor != null ? actor.getId() : null,
                            actor != null ? actor.getFullName() : "System / Routing Engine",
                            actor != null ? actor.getEmail() : null,
                            actorRole,
                            e.getAction(),
                            e.getFromStatus(),
                            e.getToStatus(),
                            e.getNotes(),
                            e.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminSystemReportResponse getSystemReport(UUID orgId, String currentUserEmail) {
        validateAdminAccess(orgId, currentUserEmail);

        long totalIssues = issueRepository.countByOrganizationId(orgId);
        long pendingReviews = issueRepository.countByOrganizationIdAndStatus(orgId, IssueStatus.SUBMITTED)
                + issueRepository.countByOrganizationIdAndStatus(orgId, IssueStatus.UNDER_REVIEW);
        long assignedAndActive = issueRepository.countByOrganizationIdAndStatus(orgId, IssueStatus.ASSIGNED)
                + issueRepository.countByOrganizationIdAndStatus(orgId, IssueStatus.IN_PROGRESS);
        long awaitingFinance = issueRepository.countByOrganizationIdAndStatus(orgId, IssueStatus.AWAITING_FINANCE);
        long awaitingVerification = issueRepository.countByOrganizationIdAndStatus(orgId, IssueStatus.RESOLVED_PENDING_VERIFICATION);
        long verifiedAndClosed = issueRepository.countByOrganizationIdAndStatus(orgId, IssueStatus.CLOSED);
        long rejectedIssues = issueRepository.countByOrganizationIdAndStatus(orgId, IssueStatus.REJECTED);
        long reopenedIssues = issueRepository.countByOrganizationIdAndStatus(orgId, IssueStatus.REOPENED);

        long slaBreached = issueRepository.countByOrganizationIdAndSlaBreachedTrue(orgId);
        double slaCompliance = totalIssues > 0
                ? Math.round(((double) (totalIssues - slaBreached) / totalIssues) * 10000.0) / 100.0
                : 100.0;

        // Status Map
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (IssueStatus s : IssueStatus.values()) {
            long count = issueRepository.countByOrganizationIdAndStatus(orgId, s);
            statusMap.put(s.name(), count);
        }

        // Priority Map
        Map<String, Long> priorityMap = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            long count = issueRepository.countByOrganizationIdAndPriority(orgId, p);
            priorityMap.put(p.name(), count);
        }

        // Department Breakdown & Metrics
        List<Department> departments = departmentRepository.findByOrganizationId(orgId);
        Map<String, Long> deptDistribution = new LinkedHashMap<>();
        List<DepartmentMetricResponse> deptMetrics = new ArrayList<>();

        for (Department dept : departments) {
            long deptTotal = issueRepository.countByOrganizationIdAndDepartmentId(orgId, dept.getId());
            deptDistribution.put(dept.getName(), deptTotal);

            List<Issue> deptIssues = issueRepository.findByOrganizationIdAndDepartmentIdOrderByCreatedAtDesc(orgId, dept.getId());
            long deptResolved = deptIssues.stream().filter(i -> i.getStatus() == IssueStatus.CLOSED).count();
            long deptActive = deptIssues.stream().filter(i -> i.getStatus() != IssueStatus.CLOSED && i.getStatus() != IssueStatus.REJECTED).count();

            double deptAvgHours = 0.0;
            List<Issue> deptClosed = deptIssues.stream().filter(i -> i.getClosedAt() != null).toList();
            if (!deptClosed.isEmpty()) {
                double hours = deptClosed.stream()
                        .mapToDouble(i -> Duration.between(i.getCreatedAt(), i.getClosedAt()).toMinutes() / 60.0)
                        .sum();
                deptAvgHours = Math.round((hours / deptClosed.size()) * 10.0) / 10.0;
            }

            deptMetrics.add(new DepartmentMetricResponse(
                    dept.getId(),
                    dept.getName(),
                    dept.getCode(),
                    deptTotal,
                    deptActive,
                    deptResolved,
                    deptAvgHours
            ));
        }

        return new AdminSystemReportResponse(
                totalIssues,
                pendingReviews,
                assignedAndActive,
                awaitingFinance,
                awaitingVerification,
                verifiedAndClosed,
                rejectedIssues,
                reopenedIssues,
                slaBreached,
                slaCompliance,
                statusMap,
                priorityMap,
                deptDistribution,
                deptMetrics
        );
    }
}
