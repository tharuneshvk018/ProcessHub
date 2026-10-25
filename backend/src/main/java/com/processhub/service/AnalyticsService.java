package com.processhub.service;

import com.processhub.domain.entity.Department;
import com.processhub.domain.entity.ExpenditureRequest;
import com.processhub.domain.entity.Issue;
import com.processhub.domain.enums.ExpenditureStatus;
import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import com.processhub.domain.repository.DepartmentRepository;
import com.processhub.domain.repository.ExpenditureRequestRepository;
import com.processhub.domain.repository.IssueRepository;
import com.processhub.dto.DepartmentMetricResponse;
import com.processhub.dto.OperationalAnalyticsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final IssueRepository issueRepository;
    private final DepartmentRepository departmentRepository;
    private final ExpenditureRequestRepository expenditureRepository;
    private final OrganizationService organizationService;

    public AnalyticsService(
            IssueRepository issueRepository,
            DepartmentRepository departmentRepository,
            ExpenditureRequestRepository expenditureRepository,
            OrganizationService organizationService) {
        this.issueRepository = issueRepository;
        this.departmentRepository = departmentRepository;
        this.expenditureRepository = expenditureRepository;
        this.organizationService = organizationService;
    }

    @Transactional(readOnly = true)
    public OperationalAnalyticsResponse getOperationalAnalytics(UUID orgId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        long totalIssues = issueRepository.countByOrganizationId(orgId);
        long resolvedIssues = issueRepository.countByOrganizationIdAndStatus(orgId, IssueStatus.CLOSED);
        long rejectedIssues = issueRepository.countByOrganizationIdAndStatus(orgId, IssueStatus.REJECTED);
        long activeIssues = Math.max(0, totalIssues - (resolvedIssues + rejectedIssues));
        long slaBreached = issueRepository.countByOrganizationIdAndSlaBreachedTrue(orgId);

        double slaCompliance = totalIssues > 0
                ? Math.round(((double) (totalIssues - slaBreached) / totalIssues) * 10000.0) / 100.0
                : 100.0;

        List<Issue> closedList = issueRepository.findByOrganizationIdAndClosedAtIsNotNull(orgId);
        double avgResolutionHours = 0.0;
        if (!closedList.isEmpty()) {
            double totalHours = closedList.stream()
                    .mapToDouble(i -> Duration.between(i.getCreatedAt(), i.getClosedAt()).toMinutes() / 60.0)
                    .sum();
            avgResolutionHours = Math.round((totalHours / closedList.size()) * 10.0) / 10.0;
        }

        // Priority breakdown
        Map<String, Long> priorityMap = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            priorityMap.put(p.name(), issueRepository.countByOrganizationIdAndPriority(orgId, p));
        }

        // Status breakdown
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (IssueStatus s : IssueStatus.values()) {
            long count = issueRepository.countByOrganizationIdAndStatus(orgId, s);
            if (count > 0) {
                statusMap.put(s.name(), count);
            }
        }

        // Department metrics
        List<Department> departments = departmentRepository.findByOrganizationId(orgId);
        List<DepartmentMetricResponse> deptMetrics = new ArrayList<>();
        for (Department dept : departments) {
            long deptTotal = issueRepository.countByOrganizationIdAndDepartmentId(orgId, dept.getId());
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

        // Top recurring assets
        List<Issue> allOrgIssues = issueRepository.findByOrganizationIdOrderByCreatedAtDesc(orgId);
        Map<String, Long> assetCounts = allOrgIssues.stream()
                .filter(i -> i.getAssetId() != null && !i.getAssetId().isBlank())
                .collect(Collectors.groupingBy(Issue::getAssetId, Collectors.counting()));

        Map<String, Long> topAssets = assetCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        // Spend summary
        List<ExpenditureRequest> allExpenditures = expenditureRepository.findByIssueOrganizationIdOrderByCreatedAtDesc(orgId);
        BigDecimal approvedSpend = allExpenditures.stream()
                .filter(e -> e.getStatus() == ExpenditureStatus.APPROVED)
                .map(ExpenditureRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingSpend = allExpenditures.stream()
                .filter(e -> e.getStatus() == ExpenditureStatus.PENDING)
                .map(ExpenditureRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OperationalAnalyticsResponse(
                totalIssues,
                activeIssues,
                resolvedIssues,
                slaBreached,
                slaCompliance,
                avgResolutionHours,
                priorityMap,
                statusMap,
                deptMetrics,
                topAssets,
                approvedSpend.setScale(2, RoundingMode.HALF_UP),
                pendingSpend.setScale(2, RoundingMode.HALF_UP)
        );
    }
}
