package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.*;
import com.processhub.domain.enums.NotificationType;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.*;
import com.processhub.dto.CreateIssueRequest;
import com.processhub.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationCenterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private IssueTimelineEventRepository timelineRepository;

    @Autowired
    private IssueAttachmentRepository attachmentRepository;

    @Autowired
    private ExpenditureRequestRepository expenditureRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private IssueCategoryRepository categoryRepository;

    @Autowired
    private OrganizationMembershipRepository membershipRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Organization organizationA;
    private Organization organizationB;
    private Department facilitiesDept;
    private IssueCategory leakCategory;
    private User supervisorUser;
    private String supervisorToken;
    private User reporterUser;
    private String reporterToken;
    private User outsideUser;
    private String outsideToken;

    @BeforeEach
    void setUp() {
        cleanUpData();

        organizationA = organizationRepository.save(new Organization(UUID.randomUUID(), "Primary Facility Org", "ORG-A"));
        organizationB = organizationRepository.save(new Organization(UUID.randomUUID(), "Secondary Facility Org", "ORG-B"));

        facilitiesDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organizationA, "Facilities & Maintenance", "FAC-01", "Facilities management"));

        leakCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organizationA, facilitiesDept, "LEAK-01", "Plumbing & Leaks",
                "Water leaks", "BURST", "MEDIUM", false));

        // Supervisor in Org A
        supervisorUser = userRepository.save(new User(
                UUID.randomUUID(), "supervisor.a@processhub.com", passwordEncoder.encode("Password123!"),
                "Facilities Supervisor", "+15550001", Role.ROLE_SUPERVISOR, "Facilities"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organizationA, supervisorUser, facilitiesDept, Role.ROLE_SUPERVISOR));
        supervisorToken = jwtTokenProvider.generateToken(supervisorUser);

        // Reporter in Org A
        reporterUser = userRepository.save(new User(
                UUID.randomUUID(), "reporter.a@processhub.com", passwordEncoder.encode("Password123!"),
                "Alice Reporter", "+15550002", Role.ROLE_REPORTER, "Operations"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organizationA, reporterUser, facilitiesDept, Role.ROLE_REPORTER));
        reporterToken = jwtTokenProvider.generateToken(reporterUser);

        // Outside user in Org B
        outsideUser = userRepository.save(new User(
                UUID.randomUUID(), "outside.b@processhub.com", passwordEncoder.encode("Password123!"),
                "Bob Outside", "+15550003", Role.ROLE_REPORTER, "External"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organizationB, outsideUser, null, Role.ROLE_REPORTER));
        outsideToken = jwtTokenProvider.generateToken(outsideUser);
    }

    @AfterEach
    void tearDown() {
        cleanUpData();
    }

    private void cleanUpData() {
        notificationRepository.deleteAll();
        expenditureRepository.deleteAll();
        attachmentRepository.deleteAll();
        timelineRepository.deleteAll();
        issueRepository.deleteAll();
        categoryRepository.deleteAll();
        membershipRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Dispatches persistent in-app notification to department supervisor upon issue submission")
    void testNotificationDispatchedOnIssueSubmission() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                facilitiesDept.getId(),
                leakCategory.getId(),
                "PIPE_BURST",
                "Burst Pipe In Lab 202",
                "High pressure water leaking onto electrical cabinets",
                "Engineering Annex",
                "Room 202",
                "ASSET-P-900",
                "CRITICAL"
        );

        // Reporter submits issue
        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referenceCode", notNullValue()));

        // Supervisor checks unread count
        mockMvc.perform(get("/api/v1/organizations/" + organizationA.getId() + "/notifications/unread-count")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount", is(1)));

        // Supervisor retrieves notifications
        mockMvc.perform(get("/api/v1/organizations/" + organizationA.getId() + "/notifications")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].notificationType", is(NotificationType.ISSUE_SUBMITTED.name())))
                .andExpect(jsonPath("$[0].read", is(false)))
                .andExpect(jsonPath("$[0].issueReferenceCode", notNullValue()))
                .andExpect(jsonPath("$[0].title", containsString("New Issue")));
    }

    @Test
    @DisplayName("Marks single notification as read and updates unread badge count")
    void testMarkSingleNotificationAsRead() throws Exception {
        Notification notification = notificationRepository.save(new Notification(
                UUID.randomUUID(),
                organizationA,
                supervisorUser,
                null,
                NotificationType.ISSUE_SUBMITTED,
                "Test Alert",
                "Please review pending item"
        ));

        // Verify initial unread count
        mockMvc.perform(get("/api/v1/organizations/" + organizationA.getId() + "/notifications/unread-count")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount", is(1)));

        // Mark as read
        mockMvc.perform(patch("/api/v1/organizations/" + organizationA.getId() + "/notifications/" + notification.getId() + "/read")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notification.getId().toString())))
                .andExpect(jsonPath("$.read", is(true)))
                .andExpect(jsonPath("$.readAt", notNullValue()));

        // Verify unread count becomes 0
        mockMvc.perform(get("/api/v1/organizations/" + organizationA.getId() + "/notifications/unread-count")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount", is(0)));
    }

    @Test
    @DisplayName("Marks all notifications as read in bulk")
    void testMarkAllNotificationsAsRead() throws Exception {
        notificationRepository.save(new Notification(
                UUID.randomUUID(), organizationA, supervisorUser, null,
                NotificationType.ISSUE_SUBMITTED, "Alert 1", "Message 1"));
        notificationRepository.save(new Notification(
                UUID.randomUUID(), organizationA, supervisorUser, null,
                NotificationType.STATUS_CHANGED, "Alert 2", "Message 2"));
        notificationRepository.save(new Notification(
                UUID.randomUUID(), organizationA, supervisorUser, null,
                NotificationType.SLA_BREACH_WARNING, "Alert 3", "Message 3"));

        mockMvc.perform(get("/api/v1/organizations/" + organizationA.getId() + "/notifications/unread-count")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount", is(3)));

        // Mark all as read
        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/notifications/mark-all-read")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("marked as read")));

        mockMvc.perform(get("/api/v1/organizations/" + organizationA.getId() + "/notifications/unread-count")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount", is(0)));
    }

    @Test
    @DisplayName("Enforces tenant isolation: Outside user cannot access or mark notifications in Org A")
    void testTenantIsolationOnNotifications() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + organizationA.getId() + "/notifications")
                        .header("Authorization", "Bearer " + outsideToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/organizations/" + organizationA.getId() + "/notifications/unread-count")
                        .header("Authorization", "Bearer " + outsideToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/notifications/mark-all-read")
                        .header("Authorization", "Bearer " + outsideToken))
                .andExpect(status().isForbidden());
    }
}
