package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.*;
import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.*;
import com.processhub.dto.*;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TechnicianWorkboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private IssueAttachmentRepository attachmentRepository;

    @Autowired
    private IssueTimelineEventRepository timelineRepository;

    @Autowired
    private ExpenditureRequestRepository expenditureRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private IssueCategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

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

    private Organization organization;
    private Department facilitiesDept;
    private IssueCategory hvacCategory;
    private User technicianBob;
    private String technicianToken;
    private User reporterAlice;
    private String reporterToken;
    private User externalUser;
    private String externalToken;

    @BeforeEach
    void setUp() {
        cleanUpData();

        organization = organizationRepository.save(new Organization(UUID.randomUUID(), "Tech Center", "TC"));
        facilitiesDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "Facilities & Engineering", "FE", "Engineering Operations"));

        hvacCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, facilitiesDept, "HVAC", "HVAC Failure",
                "Cooling/Heating", "CHILLER", "HIGH", false));

        technicianBob = userRepository.save(new User(
                UUID.randomUUID(), "bob.tech@processhub.com", passwordEncoder.encode("Pass123!"),
                "Bob Builder", "555-1111", Role.ROLE_TECHNICIAN, "Facilities"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, technicianBob, facilitiesDept, Role.ROLE_TECHNICIAN));
        technicianToken = jwtTokenProvider.generateToken(technicianBob);

        reporterAlice = userRepository.save(new User(
                UUID.randomUUID(), "alice.rep@processhub.com", passwordEncoder.encode("Pass123!"),
                "Alice Reporter", "555-2222", Role.ROLE_REPORTER, "Operations"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, reporterAlice, facilitiesDept, Role.ROLE_REPORTER));
        reporterToken = jwtTokenProvider.generateToken(reporterAlice);

        externalUser = userRepository.save(new User(
                UUID.randomUUID(), "external@processhub.com", passwordEncoder.encode("Pass123!"),
                "External Guy", "555-9999", Role.ROLE_REPORTER, "Outside"));
        externalToken = jwtTokenProvider.generateToken(externalUser);
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

    private Issue createIssue(String title, Priority priority, IssueStatus status, User assignedTech) {
        Issue issue = new Issue(
                UUID.randomUUID(), "PH-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(),
                organization, facilitiesDept, hvacCategory, reporterAlice,
                "CHILLER", title, "Issue description: " + title, "Building A", "Room 101",
                "ASSET-01", priority
        );
        issue.setStatus(status);
        issue.setAssignedTechnician(assignedTech);
        if (status == IssueStatus.ASSIGNED) {
            issue.setCurrentStage("ASSIGNED");
            issue.setResponsibleRole("ROLE_TECHNICIAN");
            issue.setNextAction("Technician start diagnostic and repair work");
        } else if (status == IssueStatus.IN_PROGRESS) {
            issue.setCurrentStage("IN_PROGRESS");
            issue.setResponsibleRole("ROLE_TECHNICIAN");
            issue.setNextAction("Technician execute repairs");
        }
        return issueRepository.save(issue);
    }

    @Test
    @DisplayName("Technician workboard returns assigned issues ordered by priority descending")
    void testWorkboardPriorityOrdering() throws Exception {
        createIssue("Low Chiller Noise", Priority.LOW, IssueStatus.ASSIGNED, technicianBob);
        createIssue("Critical Main Line Burst", Priority.CRITICAL, IssueStatus.ASSIGNED, technicianBob);
        createIssue("High Temp Warning", Priority.HIGH, IssueStatus.IN_PROGRESS, technicianBob);

        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/technician/workboard")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].priority").value("CRITICAL"))
                .andExpect(jsonPath("$[0].title").value("Critical Main Line Burst"))
                .andExpect(jsonPath("$[1].priority").value("HIGH"))
                .andExpect(jsonPath("$[1].title").value("High Temp Warning"))
                .andExpect(jsonPath("$[2].priority").value("LOW"))
                .andExpect(jsonPath("$[2].title").value("Low Chiller Noise"));
    }

    @Test
    @DisplayName("Technician workboard filters by status accurately")
    void testWorkboardStatusFilter() throws Exception {
        createIssue("Assigned Chiller 1", Priority.HIGH, IssueStatus.ASSIGNED, technicianBob);
        createIssue("Active Chiller 2", Priority.HIGH, IssueStatus.IN_PROGRESS, technicianBob);

        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/technician/workboard")
                        .param("status", "IN_PROGRESS")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Active Chiller 2"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Technician metrics return accurate count distribution across lifecycle")
    void testTechnicianMetrics() throws Exception {
        createIssue("Assigned Issue", Priority.MEDIUM, IssueStatus.ASSIGNED, technicianBob);
        createIssue("In Progress Issue", Priority.HIGH, IssueStatus.IN_PROGRESS, technicianBob);
        createIssue("Awaiting Finance Issue", Priority.HIGH, IssueStatus.AWAITING_FINANCE, technicianBob);
        createIssue("Pending Verification Issue", Priority.HIGH, IssueStatus.RESOLVED_PENDING_VERIFICATION, technicianBob);
        createIssue("Closed Issue", Priority.LOW, IssueStatus.CLOSED, technicianBob);

        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/technician/metrics")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedCount").value(1))
                .andExpect(jsonPath("$.inProgressCount").value(1))
                .andExpect(jsonPath("$.awaitingFinanceCount").value(1))
                .andExpect(jsonPath("$.resolvedPendingVerificationCount").value(1))
                .andExpect(jsonPath("$.completedCount").value(1));
    }

    @Test
    @DisplayName("Start work transitions issue from ASSIGNED to IN_PROGRESS and records timeline event")
    void testStartWorkTransitionsToInProgress() throws Exception {
        Issue issue = createIssue("Assigned Chiller", Priority.HIGH, IssueStatus.ASSIGNED, technicianBob);

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/start")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.currentStage").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.responsibleRole").value("ROLE_TECHNICIAN"))
                .andExpect(jsonPath("$.timeline[0].action").value("STATUS_CHANGED"))
                .andExpect(jsonPath("$.timeline[0].toStatus").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Technician logs progress updates with percentage and notes")
    void testUpdateProgress() throws Exception {
        Issue issue = createIssue("In Progress Motor", Priority.HIGH, IssueStatus.IN_PROGRESS, technicianBob);

        UpdateProgressRequest req = new UpdateProgressRequest("Replaced cooling pump and running pressure diagnostics", 75);

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/progress")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeline[0].action").value("PROGRESS_UPDATED"))
                .andExpect(jsonPath("$.timeline[0].notes").value(containsString("[75% Complete] Replaced cooling pump")));
    }

    @Test
    @DisplayName("Submit resolution transitions issue to RESOLVED_PENDING_VERIFICATION with labor and parts details")
    void testSubmitResolution() throws Exception {
        Issue issue = createIssue("In Progress Compressor", Priority.HIGH, IssueStatus.IN_PROGRESS, technicianBob);

        SubmitResolutionRequest req = new SubmitResolutionRequest(
                "Overhauled compressor valve seals and restored baseline refrigerant pressure",
                "Debris ingress causing seal breakdown",
                new BigDecimal("4.5"),
                "2x Valve Gaskets (Part #VG-992)",
                "https://storage.processhub.internal/evidence/comp-repair.jpg"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/resolve")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED_PENDING_VERIFICATION"))
                .andExpect(jsonPath("$.currentStage").value("RESOLVED_PENDING_VERIFICATION"))
                .andExpect(jsonPath("$.responsibleRole").value("ROLE_SUPERVISOR"))
                .andExpect(jsonPath("$.blockingReason").value(containsString("Pending independent verification")))
                .andExpect(jsonPath("$.workSummary").value(containsString("Overhauled compressor valve seals")))
                .andExpect(jsonPath("$.laborHours").value(4.5))
                .andExpect(jsonPath("$.partsUsed").value(containsString("Valve Gaskets")))
                .andExpect(jsonPath("$.timeline[0].action").value("SUBMITTED_FOR_VERIFICATION"));
    }

    @Test
    @DisplayName("Cannot submit resolution when pending expenditure request exists")
    void testSubmitResolutionBlockedByPendingExpenditures() throws Exception {
        Issue issue = createIssue("In Progress Chiller", Priority.HIGH, IssueStatus.IN_PROGRESS, technicianBob);

        // Add pending expenditure
        ExpenditureRequest exp = new ExpenditureRequest();
        exp.setId(UUID.randomUUID());
        exp.setIssue(issue);
        exp.setRequestedBy(technicianBob);
        exp.setAmount(new BigDecimal("350.00"));
        exp.setJustification("Need replacement compressor coil");
        exp.setStatus(com.processhub.domain.enums.ExpenditureStatus.PENDING);
        expenditureRepository.save(exp);

        SubmitResolutionRequest req = new SubmitResolutionRequest(
                "Attempted early closure", null, new BigDecimal("1.0"), null, null);

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/resolve")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Cannot resolve issue with pending expenditure requests")));
    }

    @Test
    @DisplayName("External user without organization membership is forbidden from accessing technician workboard")
    void testExternalUserForbiddenFromWorkboard() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/technician/workboard")
                        .header("Authorization", "Bearer " + externalToken))
                .andExpect(status().isForbidden());
    }
}
