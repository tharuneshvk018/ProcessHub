package com.processhub.controller;

import com.processhub.domain.enums.ExpenditureStatus;
import com.processhub.dto.ExpenditureResponse;
import com.processhub.dto.FinanceMetricsResponse;
import com.processhub.service.FinanceWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/finance")
public class FinanceController {

    private final FinanceWorkflowService financeWorkflowService;

    public FinanceController(FinanceWorkflowService financeWorkflowService) {
        this.financeWorkflowService = financeWorkflowService;
    }

    @GetMapping("/queue")
    public ResponseEntity<List<ExpenditureResponse>> getFinanceQueue(
            @PathVariable UUID orgId,
            @RequestParam(required = false) ExpenditureStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ExpenditureResponse> queue = financeWorkflowService.getFinanceQueue(
                orgId, status, userDetails.getUsername());
        return ResponseEntity.ok(queue);
    }

    @GetMapping("/metrics")
    public ResponseEntity<FinanceMetricsResponse> getFinanceMetrics(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        FinanceMetricsResponse metrics = financeWorkflowService.getFinanceMetrics(
                orgId, userDetails.getUsername());
        return ResponseEntity.ok(metrics);
    }
}
