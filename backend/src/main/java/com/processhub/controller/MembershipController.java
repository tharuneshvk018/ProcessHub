package com.processhub.controller;

import com.processhub.dto.CreateMembershipRequest;
import com.processhub.dto.MembershipResponse;
import com.processhub.service.MembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping("/organizations/{orgId}/memberships")
    public ResponseEntity<MembershipResponse> assignMembership(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateMembershipRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MembershipResponse response = membershipService.assignMembership(orgId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/organizations/{orgId}/memberships")
    public ResponseEntity<List<MembershipResponse>> getOrganizationMemberships(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<MembershipResponse> responses = membershipService.getMembershipsForOrganization(orgId, userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/users/me/memberships")
    public ResponseEntity<List<MembershipResponse>> getMyMemberships(@AuthenticationPrincipal UserDetails userDetails) {
        List<MembershipResponse> responses = membershipService.getUserMemberships(userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }
}
