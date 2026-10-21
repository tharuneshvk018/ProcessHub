package com.processhub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestProtectedController {

    @GetMapping("/user")
    public ResponseEntity<Map<String, String>> userEndpoint() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Authenticated user access granted"
        ));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminEndpoint() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Administrator authorization verified"
        ));
    }

    @GetMapping("/supervisor")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, String>> supervisorEndpoint() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Supervisor authorization verified"
        ));
    }

    @GetMapping("/technician")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<Map<String, String>> technicianEndpoint() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Technician authorization verified"
        ));
    }

    @GetMapping("/finance")
    @PreAuthorize("hasRole('FINANCE_APPROVER')")
    public ResponseEntity<Map<String, String>> financeEndpoint() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Finance Approver authorization verified"
        ));
    }
}
