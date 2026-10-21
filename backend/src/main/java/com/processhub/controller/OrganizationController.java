package com.processhub.controller;

import com.processhub.dto.CreateOrganizationRequest;
import com.processhub.dto.OrganizationResponse;
import com.processhub.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> createOrganization(@Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> listOrganizations(@AuthenticationPrincipal UserDetails userDetails) {
        List<OrganizationResponse> responses = organizationService.listOrganizationsForUser(userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<OrganizationResponse> getOrganization(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrganizationResponse response = organizationService.getOrganization(orgId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
