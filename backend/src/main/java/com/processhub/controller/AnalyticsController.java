package com.processhub.controller;

import com.processhub.dto.OperationalAnalyticsResponse;
import com.processhub.service.AnalyticsService;
import com.processhub.service.OrganizationService;
import com.processhub.service.SlaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SlaService slaService;
    private final OrganizationService organizationService;

    public AnalyticsController(
            AnalyticsService analyticsService,
            SlaService slaService,
            OrganizationService organizationService) {
        this.analyticsService = analyticsService;
        this.slaService = slaService;
        this.organizationService = organizationService;
    }

    @GetMapping("/overview")
    public ResponseEntity<OperationalAnalyticsResponse> getOperationalAnalytics(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        OperationalAnalyticsResponse response = analyticsService.getOperationalAnalytics(
                orgId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sla-escalate")
    public ResponseEntity<Map<String, Object>> triggerSlaEscalation(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        organizationService.validateUserOrganizationAccess(orgId, userDetails.getUsername());
        int escalatedCount = slaService.checkAndTriggerSlaEscalations(orgId);
        return ResponseEntity.ok(Map.of(
                "escalatedCount", escalatedCount,
                "message", "SLA breach check and escalation completed"
        ));
    }
}
