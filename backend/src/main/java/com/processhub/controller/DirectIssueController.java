package com.processhub.controller;

import com.processhub.dto.IssueResponse;
import com.processhub.service.IssueService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/issues")
public class DirectIssueController {

    private final IssueService issueService;

    public DirectIssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping("/ref/{referenceCode}")
    public ResponseEntity<IssueResponse> getIssueByReference(
            @PathVariable String referenceCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        IssueResponse response = issueService.getIssueByReferenceDirect(referenceCode, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
