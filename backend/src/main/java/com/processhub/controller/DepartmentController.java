package com.processhub.controller;

import com.processhub.dto.CreateDepartmentRequest;
import com.processhub.dto.DepartmentResponse;
import com.processhub.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateDepartmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        DepartmentResponse response = departmentService.createDepartment(orgId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getDepartments(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<DepartmentResponse> responses = departmentService.getDepartmentsForOrganization(orgId, userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponse> getDepartment(
            @PathVariable UUID orgId,
            @PathVariable UUID departmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        DepartmentResponse response = departmentService.getDepartment(orgId, departmentId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
