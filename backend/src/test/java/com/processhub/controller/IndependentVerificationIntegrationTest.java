package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.*;
import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.*;
import com.processhub.dto.SubmitResolutionRequest;
import com.processhub.dto.VerifyResolutionRequest;
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
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IndependentVerificationIntegrationTest {

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
    private IssueCategory plumbingCategory;
    private User technicianBob;
    private String technicianToken;
    private User supervisorMarcus;
    private String supervisorToken;
    private User reporterAlice;
    private String reporterToken;
    private User externalUser;
    private String externalToken;

    @BeforeEach
    void setUp() {
        cleanUpData();

        organization = organizationRepository.save(new Organization(UUID.randomUUID(), "Enterprise Campus", "EC"));
        facilitiesDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "Facilities", "FAC", "Campus Facilities"));

        plumbingCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, facilitiesDept, "WATER_LEAK", "Water Pipe Leak",
                "Plumbing", "VALVE", "HIGH", false));

        technicianBob = userRepository.save(new User(
                UUID.randomUUID(), "bob.builder@processhub.com", passwordEncoder.encode("Pass123!"),
                "Bob Builder", "555-0101", Role.ROLE_TECHNICIAN, "Facilities"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, technicianBob, facilitiesDept, Role.ROLE_TECHNICIAN));
        technicianToken = jwtTokenProvider.generateToken(technicianBob);

        supervisorMarcus = userRepository.save(new User(
                UUID.randomUUID(), "marcus.sup@processhub.com", passwordEncoder.encode("Pass123!"),
                "Marcus Aurelius", "555-0102", Role.ROLE_SUPERVISOR, "Facilities"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, supervisorMarcus, facilitiesDept, Role.ROLE_SUPERVISOR));
        supervisorToken = jwtTokenProvider.generateToken(supervisorMarcus);

        reporterAlice = userRepository.save(new User(
                UUID.randomUUID(), "alice.rep@processhub.com", passwordEncoder.encode("Pass123!"),
                "Alice Wonder", "555-0103", Role.ROLE_REPORTER, "Biology Lab"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, reporterAlice, facilitiesDept, Role.ROLE_REPORTER));
        reporterToken = jwtTokenProvider.generateToken(reporterAlice);

        externalUser = userRepository.save(new User(
                UUID.randomUUID(), "unaffiliated@processhub.com", passwordEncoder.encode("Pass123!"),
                "Eve Unaffiliated", "555-0199", Role.ROLE_REPORTER, "Guest"));
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

    private Issue createResolvedPendingVerificationIssue() {
        Issue issue = new Issue(
                UUID.randomUUID(), "PH-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(),
                organization, facilitiesDept, plumbingCategory, reporterAlice,
                "VALVE", "Leaking Pressure Relief Valve", "Relief valve dripping into drip pan",
                "Lab Wing 3", "Room 304", "ASSET-90", Priority.HIGH
        );
        issue.setStatus(IssueStatus.RESOLVED_PENDING_VERIFICATION);
        issue.setAssignedTechnician(technicianBob);
        issue.setCurrentStage("RESOLVED_PENDING_VERIFICATION");
        issue.setResponsibleRole("ROLE_SUPERVISOR");
        issue.setBlockingReason("Pending independent verification and sign-off");
        issue.setNextAction("Supervisor or Reporter quality and completion sign-off");
        issue.setWorkSummary("Replaced faulty pressure diaphragm and torque-calibrated valve bolts");
        issue.setRootCause("Material fatigue on rubber diaphragm");
        issue.setLaborHours(new BigDecimal("2.5"));
        issue.setPartsUsed("1x Nitrile Diaphragm (Part #ND-104)");
        issue.setResolvedAt(Instant.now());
        return issueRepository.save(issue);
    }

    @Test
    @DisplayName("Separation of duties: Technician who performed resolution cannot independently verify own work")
    void testTechnicianCannotVerifyOwnWork() throws Exception {
        Issue issue = createResolvedPendingVerificationIssue();

        VerifyResolutionRequest req = new VerifyResolutionRequest(true, "Self sign-off attempt");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/verify")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("Technician cannot independently verify their own work")));
    }

    @Test
    @DisplayName("Supervisor independent verification passes and closes the issue permanently")
    void testSupervisorVerificationPassClosesIssue() throws Exception {
        Issue issue = createResolvedPendingVerificationIssue();

        VerifyResolutionRequest req = new VerifyResolutionRequest(
                true, "Physical on-site inspection completed. Zero fluid leakage detected under nominal pressure."
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/verify")
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.currentStage").value("CLOSED"))
                .andExpect(jsonPath("$.responsibleRole").value("NONE"))
                .andExpect(jsonPath("$.nextAction").value(containsString("None (Resolution Complete)")))
                .andExpect(jsonPath("$.blockingReason").doesNotExist())
                .andExpect(jsonPath("$.explainableProgress.blocked").value(false))
                .andExpect(jsonPath("$.verifiedByName").value("Marcus Aurelius"))
                .andExpect(jsonPath("$.verificationNotes").value(containsString("Physical on-site inspection completed")))
                .andExpect(jsonPath("$.timeline[0].action").value("VERIFICATION_PASSED"))
                .andExpect(jsonPath("$.timeline[0].toStatus").value("CLOSED"));
    }

    @Test
    @DisplayName("Reporter can independently verify resolution and sign-off on closure")
    void testReporterVerificationPassClosesIssue() throws Exception {
        Issue issue = createResolvedPendingVerificationIssue();

        VerifyResolutionRequest req = new VerifyResolutionRequest(
                true, "Confirmed by lab reporter: Room 304 sink area is clean and dry."
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/verify")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.verifiedByName").value("Alice Wonder"));
    }

    @Test
    @DisplayName("Verification rejection reopens issue to IN_PROGRESS and assigns rework to technician")
    void testVerificationRejectionReopensToInProgress() throws Exception {
        Issue issue = createResolvedPendingVerificationIssue();

        VerifyResolutionRequest req = new VerifyResolutionRequest(
                false, "Pressure gauge shows micro-leak after 10-minute hold. Re-torque or seal replacement required."
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/verify")
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.currentStage").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.responsibleRole").value("ROLE_TECHNICIAN"))
                .andExpect(jsonPath("$.blockingReason").value(containsString("Verification rejected: Pressure gauge shows micro-leak")))
                .andExpect(jsonPath("$.nextAction").value(containsString("Technician address verification failure notes")))
                .andExpect(jsonPath("$.timeline[0].action").value("VERIFICATION_FAILED"))
                .andExpect(jsonPath("$.timeline[0].toStatus").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Cannot verify issue that is not in RESOLVED_PENDING_VERIFICATION state")
    void testCannotVerifyIssueNotInPendingVerificationState() throws Exception {
        Issue issue = new Issue(
                UUID.randomUUID(), "PH-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(),
                organization, facilitiesDept, plumbingCategory, reporterAlice,
                "VALVE", "Active Issue", "Active in progress", "Lab", "101", null, Priority.HIGH
        );
        issue.setStatus(IssueStatus.IN_PROGRESS);
        issue = issueRepository.save(issue);

        VerifyResolutionRequest req = new VerifyResolutionRequest(true, "Premature verification attempt");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/verify")
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Cannot verify issue in status: IN_PROGRESS")));
    }

    @Test
    @DisplayName("External user without organization membership is forbidden from verifying issues")
    void testExternalUserForbiddenFromVerifying() throws Exception {
        Issue issue = createResolvedPendingVerificationIssue();

        VerifyResolutionRequest req = new VerifyResolutionRequest(true, "Unauthorized external attempt");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/verify")
                        .header("Authorization", "Bearer " + externalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
