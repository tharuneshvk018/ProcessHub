package com.processhub.controller;

import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import com.processhub.dto.IssueResponse;
import com.processhub.dto.TechnicianMetricsResponse;
import com.processhub.service.TechnicianWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/technician")
public class TechnicianController {

    private final TechnicianWorkflowService technicianWorkflowService;

    public TechnicianController(TechnicianWorkflowService technicianWorkflowService) {
        this.technicianWorkflowService = technicianWorkflowService;
    }

    @GetMapping("/workboard")
    public ResponseEntity<List<IssueResponse>> getTechnicianWorkboard(
            @PathVariable UUID orgId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Priority priority,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<IssueResponse> workboard = technicianWorkflowService.getTechnicianWorkboard(
                orgId, status, priority, userDetails.getUsername());
        return ResponseEntity.ok(workboard);
    }

    @GetMapping("/metrics")
    public ResponseEntity<TechnicianMetricsResponse> getTechnicianMetrics(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        TechnicianMetricsResponse metrics = technicianWorkflowService.getTechnicianMetrics(
                orgId, userDetails.getUsername());
        return ResponseEntity.ok(metrics);
    }
}
