package com.processhub.controller;

import com.processhub.dto.NotificationResponse;
import com.processhub.dto.UnreadCountResponse;
import com.processhub.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @PathVariable UUID orgId,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationResponse> notifications = notificationService.getUserNotifications(
                orgId, unreadOnly, userDetails.getUsername());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UnreadCountResponse count = notificationService.getUnreadCount(orgId, userDetails.getUsername());
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID orgId,
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        NotificationResponse response = notificationService.markAsRead(orgId, notificationId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @PathVariable UUID orgId,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(orgId, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }
}
