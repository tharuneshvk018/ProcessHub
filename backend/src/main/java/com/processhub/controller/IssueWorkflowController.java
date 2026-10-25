package com.processhub.controller;

import com.processhub.dto.*;
import com.processhub.service.FinanceWorkflowService;
import com.processhub.service.SupervisorWorkflowService;
import com.processhub.service.TechnicianWorkflowService;
import com.processhub.service.VerificationWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/issues/{issueId}")
public class IssueWorkflowController {

    private final SupervisorWorkflowService supervisorWorkflowService;
    private final TechnicianWorkflowService technicianWorkflowService;
    private final FinanceWorkflowService financeWorkflowService;
    private final VerificationWorkflowService verificationWorkflowService;

    public IssueWorkflowController(
            SupervisorWorkflowService supervisorWorkflowService,
            TechnicianWorkflowService technicianWorkflowService,
            FinanceWorkflowService financeWorkflowService,
            VerificationWorkflowService verificationWorkflowService) {
        this.supervisorWorkflowService = supervisorWorkflowService;
        this.technicianWorkflowService = technicianWorkflowService;
        this.financeWorkflowService = financeWorkflowService;
        this.verificationWorkflowService = verificationWorkflowService;
    }

    @PostMapping("/route")
    public ResponseEntity<IssueResponse> evaluateAndApplyRouting(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = supervisorWorkflowService.evaluateAndApplyRouting(
                orgId, issueId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/review")
    public ResponseEntity<IssueResponse> startReview(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = supervisorWorkflowService.startReview(
                orgId, issueId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve")
    public ResponseEntity<IssueResponse> approveIssue(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @Valid @RequestBody(required = false) ApproveIssueRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ApproveIssueRequest req = request != null ? request : new ApproveIssueRequest();
        IssueResponse response = supervisorWorkflowService.approveIssue(
                orgId, issueId, req, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reject")
    public ResponseEntity<IssueResponse> rejectIssue(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @Valid @RequestBody RejectIssueRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = supervisorWorkflowService.rejectIssue(
                orgId, issueId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-info")
    public ResponseEntity<IssueResponse> requestInformation(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @Valid @RequestBody RfiRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = supervisorWorkflowService.requestInformation(
                orgId, issueId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/provide-info")
    public ResponseEntity<IssueResponse> provideInformation(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @Valid @RequestBody ProvideInfoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = supervisorWorkflowService.provideInformation(
                orgId, issueId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    public ResponseEntity<IssueResponse> assignTechnician(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @Valid @RequestBody AssignTechnicianRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = supervisorWorkflowService.assignTechnician(
                orgId, issueId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    public ResponseEntity<IssueResponse> startWork(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = technicianWorkflowService.startWork(
                orgId, issueId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/progress")
    public ResponseEntity<IssueResponse> updateProgress(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @Valid @RequestBody UpdateProgressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = technicianWorkflowService.updateProgress(
                orgId, issueId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resolve")
    public ResponseEntity<IssueResponse> submitResolution(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @Valid @RequestBody SubmitResolutionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = technicianWorkflowService.submitResolution(
                orgId, issueId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/expenditures")
    public ResponseEntity<IssueResponse> requestExpenditure(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @Valid @RequestBody CreateExpenditureRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = financeWorkflowService.requestExpenditure(
                orgId, issueId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expenditures")
    public ResponseEntity<List<ExpenditureResponse>> getIssueExpenditures(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ExpenditureResponse> response = financeWorkflowService.getIssueExpenditures(
                orgId, issueId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/expenditures/{expenditureId}/decision")
    public ResponseEntity<IssueResponse> decideExpenditure(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @PathVariable UUID expenditureId,
            @Valid @RequestBody DecideExpenditureRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = financeWorkflowService.decideExpenditure(
                orgId, issueId, expenditureId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<IssueResponse> verifyResolution(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @Valid @RequestBody VerifyResolutionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = verificationWorkflowService.verifyResolution(
                orgId, issueId, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
