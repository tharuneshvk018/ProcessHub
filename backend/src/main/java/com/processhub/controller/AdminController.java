package com.processhub.controller;

import com.processhub.dto.AdminActivityResponse;
import com.processhub.dto.AdminSystemReportResponse;
import com.processhub.dto.AdminUserSummaryResponse;
import com.processhub.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserSummaryResponse>> getOrganizationUsers(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AdminUserSummaryResponse> response = adminService.getOrganizationUsers(orgId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activities")
    public ResponseEntity<List<AdminActivityResponse>> getOrganizationActivities(
            @PathVariable UUID orgId,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AdminActivityResponse> response = adminService.getOrganizationActivities(orgId, limit, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/system-report")
    public ResponseEntity<AdminSystemReportResponse> getSystemReport(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AdminSystemReportResponse response = adminService.getSystemReport(orgId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
