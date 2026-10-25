package com.processhub.service;

import com.processhub.domain.entity.*;
import com.processhub.domain.enums.NotificationType;
import com.processhub.domain.repository.NotificationRepository;
import com.processhub.domain.repository.OrganizationRepository;
import com.processhub.domain.repository.UserRepository;
import com.processhub.dto.NotificationResponse;
import com.processhub.dto.UnreadCountResponse;
import com.processhub.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationService organizationService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            OrganizationService organizationService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.organizationService = organizationService;
    }

    @Transactional
    public Notification createNotification(UUID orgId, User recipient, Issue issue,
                                           NotificationType type, String title, String message) {
        Organization org = issue != null ? issue.getOrganization() : (orgId != null ? organizationRepository.findById(orgId).orElse(null) : null);

        Notification notification = new Notification(
                UUID.randomUUID(),
                org,
                recipient,
                issue,
                type,
                title,
                message
        );
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(UUID orgId, boolean unreadOnly, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        List<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationRepository.findByOrganizationIdAndRecipientIdAndReadFalseOrderByCreatedAtDesc(
                    orgId, currentUser.getId());
        } else {
            notifications = notificationRepository.findByOrganizationIdAndRecipientIdOrderByCreatedAtDesc(
                    orgId, currentUser.getId());
        }

        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID orgId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        long count = notificationRepository.countByOrganizationIdAndRecipientIdAndReadFalse(
                orgId, currentUser.getId());

        return new UnreadCountResponse(count);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID orgId, UUID notificationId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        Notification notification = notificationRepository.findByIdAndOrganizationIdAndRecipientId(
                notificationId, orgId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }

        return mapToResponse(notification);
    }

    @Transactional
    public void markAllAsRead(UUID orgId, String currentUserEmail) {
        organizationService.validateUserOrganizationAccess(orgId, currentUserEmail);

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        List<Notification> unreadList = notificationRepository
                .findByOrganizationIdAndRecipientIdAndReadFalseOrderByCreatedAtDesc(orgId, currentUser.getId());

        Instant now = Instant.now();
        for (Notification n : unreadList) {
            n.setRead(true);
            n.setReadAt(now);
        }
        notificationRepository.saveAll(unreadList);
    }

    public NotificationResponse mapToResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getOrganization().getId(),
                n.getRecipient().getId(),
                n.getIssue() != null ? n.getIssue().getId() : null,
                n.getIssue() != null ? n.getIssue().getReferenceCode() : null,
                n.getNotificationType().name(),
                n.getNotificationType().getDisplayName(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt(),
                n.getReadAt()
        );
    }
}
