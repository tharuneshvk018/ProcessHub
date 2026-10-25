package com.processhub.service;

import com.processhub.domain.entity.Issue;
import com.processhub.domain.enums.RecurrenceLevel;
import com.processhub.domain.repository.IssueRepository;
import com.processhub.dto.RecurrenceIntelligenceResponse;
import com.processhub.dto.RecurrenceSummaryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecurrenceIntelligenceService {

    private final IssueRepository issueRepository;

    public RecurrenceIntelligenceService(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    @Transactional(readOnly = true)
    public RecurrenceIntelligenceResponse analyzeRecurrence(UUID orgId, Issue currentIssue) {
        if (currentIssue == null || orgId == null) {
            return new RecurrenceIntelligenceResponse(0, 0, 0, RecurrenceLevel.NONE.name(), false,
                    "No issue data available for recurrence analysis", Collections.emptyList());
        }

        Instant ninetyDaysAgo = Instant.now().minus(90, ChronoUnit.DAYS);

        // 1. Asset ID match
        List<Issue> assetMatches = Collections.emptyList();
        if (currentIssue.getAssetId() != null && !currentIssue.getAssetId().trim().isEmpty()) {
            assetMatches = issueRepository.findByOrganizationIdAndAssetIdAndCreatedAtAfterAndIdNot(
                    orgId, currentIssue.getAssetId().trim(), ninetyDaysAgo, currentIssue.getId());
        }

        // 2. Location & Category match
        List<Issue> locationMatches = Collections.emptyList();
        if (currentIssue.getLocationBuilding() != null && currentIssue.getLocationRoom() != null && currentIssue.getCategory() != null) {
            locationMatches = issueRepository.findByOrganizationIdAndLocationBuildingAndLocationRoomAndCategoryIdAndCreatedAtAfterAndIdNot(
                    orgId, currentIssue.getLocationBuilding().trim(), currentIssue.getLocationRoom().trim(),
                    currentIssue.getCategory().getId(), ninetyDaysAgo, currentIssue.getId());
        }

        // Combine and deduplicate
        Map<UUID, Issue> uniquePastIssues = new LinkedHashMap<>();
        for (Issue issue : assetMatches) {
            uniquePastIssues.put(issue.getId(), issue);
        }
        for (Issue issue : locationMatches) {
            uniquePastIssues.put(issue.getId(), issue);
        }

        long assetCount = assetMatches.size();
        long locationCount = locationMatches.size();
        long totalRecurrences = uniquePastIssues.size();

        RecurrenceLevel level;
        boolean chronic;
        String recommendation;

        if (totalRecurrences == 0) {
            level = RecurrenceLevel.NONE;
            chronic = false;
            recommendation = "No prior recurring incidents detected for this asset or location within the last 90 days.";
        } else if (totalRecurrences == 1) {
            level = RecurrenceLevel.LOW;
            chronic = false;
            recommendation = "1 isolated prior incident recorded within the last 90 days. Standard diagnostic and resolution protocols apply.";
        } else if (totalRecurrences == 2) {
            level = RecurrenceLevel.ELEVATED;
            chronic = true;
            recommendation = "Elevated recurrence: 2 similar incidents recorded in the last 90 days. Recommend in-depth diagnostics to identify underlying root cause.";
        } else {
            level = RecurrenceLevel.CHRONIC_REPEAT;
            chronic = true;
            recommendation = "Chronic repeat failure: " + totalRecurrences + " similar incidents recorded in the last 90 days. " +
                    "Recommend comprehensive replacement or structural overhaul rather than routine patch repair.";
        }

        List<RecurrenceSummaryResponse> relatedSummaries = uniquePastIssues.values().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(issue -> new RecurrenceSummaryResponse(
                        issue.getId(),
                        issue.getReferenceCode(),
                        issue.getTitle(),
                        issue.getStatus().name(),
                        issue.getCreatedAt(),
                        issue.getResolvedAt(),
                        issue.getAssignedTechnician() != null ? issue.getAssignedTechnician().getFullName() : "Unassigned",
                        issue.getWorkSummary()
                ))
                .collect(Collectors.toList());

        return new RecurrenceIntelligenceResponse(
                totalRecurrences,
                assetCount,
                locationCount,
                level.name(),
                chronic,
                recommendation,
                relatedSummaries
        );
    }
}
