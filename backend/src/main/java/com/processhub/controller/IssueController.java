package com.processhub.controller;

import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import com.processhub.dto.*;
import com.processhub.service.AttachmentService;
import com.processhub.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/issues")
public class IssueController {

    private final IssueService issueService;
    private final AttachmentService attachmentService;

    public IssueController(IssueService issueService, AttachmentService attachmentService) {
        this.issueService = issueService;
        this.attachmentService = attachmentService;
    }

    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateIssueRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = issueService.createIssue(orgId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<IssueResponse>> getIssues(
            @PathVariable UUID orgId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<IssueResponse> responses = issueService.getOrganizationIssues(
                orgId, status, departmentId, priority, search, userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my")
    public ResponseEntity<List<IssueResponse>> getMyReports(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<IssueResponse> responses = issueService.getMyReports(orgId, userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-reports")
    public ResponseEntity<List<IssueResponse>> getMyReportsAlias(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<IssueResponse> responses = issueService.getMyReports(orgId, userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/reporter/dashboard")
    public ResponseEntity<ReporterDashboardResponse> getReporterDashboard(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ReporterDashboardResponse response = issueService.getReporterDashboard(orgId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{issueId}")
    public ResponseEntity<IssueResponse> getIssue(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = issueService.getIssue(orgId, issueId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{issueId}/recurrence")
    public ResponseEntity<RecurrenceIntelligenceResponse> getIssueRecurrence(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse issue = issueService.getIssue(orgId, issueId, userDetails.getUsername());
        return ResponseEntity.ok(issue.getRecurrenceIntelligence());
    }

    @GetMapping("/ref/{referenceCode}")
    public ResponseEntity<IssueResponse> getIssueByReference(
            @PathVariable UUID orgId,
            @PathVariable String referenceCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = issueService.getIssueByReference(orgId, referenceCode, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{issueId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        AttachmentResponse response = attachmentService.storeAttachment(orgId, issueId, file, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{issueId}/attachments")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AttachmentResponse> responses = attachmentService.getAttachmentsForIssue(orgId, issueId, userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{issueId}/attachments/{attachmentId}")
    public ResponseEntity<ByteArrayResource> downloadAttachment(
            @PathVariable UUID orgId,
            @PathVariable UUID issueId,
            @PathVariable UUID attachmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AttachmentDownloadResponse data = attachmentService.getAttachmentData(orgId, issueId, attachmentId, userDetails.getUsername());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(data.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + data.getFileName() + "\"")
                .contentLength(data.getFileSize())
                .body(new ByteArrayResource(data.getData()));
    }
}
