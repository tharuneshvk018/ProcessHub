package com.processhub.controller;

import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import com.processhub.dto.IssueResponse;
import com.processhub.dto.SupervisorMetricsResponse;
import com.processhub.dto.TechnicianSummaryResponse;
import com.processhub.service.SupervisorWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/supervisor")
public class SupervisorController {

    private final SupervisorWorkflowService supervisorWorkflowService;

    public SupervisorController(SupervisorWorkflowService supervisorWorkflowService) {
        this.supervisorWorkflowService = supervisorWorkflowService;
    }

    @GetMapping("/queue")
    public ResponseEntity<List<IssueResponse>> getSupervisorQueue(
            @PathVariable UUID orgId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) Priority priority,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<IssueResponse> queue = supervisorWorkflowService.getSupervisorQueue(
                orgId, departmentId, status, priority, userDetails.getUsername());
        return ResponseEntity.ok(queue);
    }

    @GetMapping("/metrics")
    public ResponseEntity<SupervisorMetricsResponse> getSupervisorMetrics(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        SupervisorMetricsResponse metrics = supervisorWorkflowService.getSupervisorMetrics(
                orgId, userDetails.getUsername());
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/departments/{departmentId}/technicians")
    public ResponseEntity<List<TechnicianSummaryResponse>> getDepartmentTechnicians(
            @PathVariable UUID orgId,
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TechnicianSummaryResponse> technicians = supervisorWorkflowService.getDepartmentTechnicians(
                orgId, departmentId, userDetails.getUsername());
        return ResponseEntity.ok(technicians);
    }
}
