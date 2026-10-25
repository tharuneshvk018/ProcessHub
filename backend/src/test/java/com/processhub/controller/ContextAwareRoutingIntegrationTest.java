package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.*;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContextAwareRoutingIntegrationTest {

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
    private Department unstaffedDept;
    private IssueCategory leakCategory;
    private IssueCategory unstaffedCategory;
    private User supervisorUser;
    private String supervisorToken;
    private User reporterUser;
    private String reporterToken;

    @BeforeEach
    void setUp() {
        cleanUpData();

        organization = organizationRepository.save(new Organization(UUID.randomUUID(), "Central Campus", "CENTRAL"));

        facilitiesDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "Facilities & Maintenance", "FAC-01", "Facilities"));
        unstaffedDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "Specialized Cryo Lab", "CRYO-01", "No supervisor assigned yet"));

        leakCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, facilitiesDept, "PIPE_LEAK", "Pipe Leak",
                "Water leak", "BURST", "MEDIUM", false));
        unstaffedCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, unstaffedDept, "CRYO_LEAK", "Cryo Leak",
                "Nitrogen leak", "VALVE", "HIGH", false));

        supervisorUser = userRepository.save(new User(
                UUID.randomUUID(), "supervisor@processhub.com", passwordEncoder.encode("Pass123!"),
                "Chief Supervisor", "555-9000", Role.ROLE_SUPERVISOR, "Facilities"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, supervisorUser, facilitiesDept, Role.ROLE_SUPERVISOR));
        supervisorToken = jwtTokenProvider.generateToken(supervisorUser);

        reporterUser = userRepository.save(new User(
                UUID.randomUUID(), "reporter@processhub.com", passwordEncoder.encode("Pass123!"),
                "Jane Doe", "555-9001", Role.ROLE_REPORTER, "Admin"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, reporterUser, facilitiesDept, Role.ROLE_REPORTER));
        reporterToken = jwtTokenProvider.generateToken(reporterUser);
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
    @DisplayName("Context-aware routing routes issue to active department supervisor queue")
    void testRoutingToActiveSupervisor() throws Exception {
        CreateIssueRequest req = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), "BURST",
                "Water leaking under sink in faculty lounge", "Moderate leak pooling on tiled floor.",
                "Faculty Hall", "Room 102", null, "MEDIUM");

        MvcResult result = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        // Evaluate context routing
        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/route")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStage").value("UNDER_SUPERVISOR_REVIEW"))
                .andExpect(jsonPath("$.responsibleRole").value("ROLE_SUPERVISOR"))
                .andExpect(jsonPath("$.nextAction", containsString("Supervisor will review report")));
    }

    @Test
    @DisplayName("Critical priority issue triggers emergency supervisor routing prompt")
    void testCriticalPriorityEmergencyRouting() throws Exception {
        CreateIssueRequest req = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), "BURST",
                "High pressure steam pipe rupture in central utility plant",
                "Dangerous steam escaping into active corridor. Immediate safety hazard.",
                "Utility Plant", "Boiler Room", null, "CRITICAL");

        MvcResult result = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/route")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andExpect(jsonPath("$.currentStage").value("UNDER_SUPERVISOR_REVIEW"))
                .andExpect(jsonPath("$.blockingReason", containsString("Critical Priority")))
                .andExpect(jsonPath("$.nextAction", containsString("expedite triage and assign emergency technician")));
    }

    @Test
    @DisplayName("Department with no supervisor routes to UNASSIGNED_TRIAGE exception state and logs audit record")
    void testUnassignedTriageExceptionHandling() throws Exception {
        CreateIssueRequest req = new CreateIssueRequest(
                unstaffedDept.getId(), unstaffedCategory.getId(), "VALVE",
                "Nitrogen tank regulator leaking in unstaffed cryo facility",
                "Frost accumulation on valve regulator.", "Cryo Complex", "Bay 3", null, "HIGH");

        MvcResult result = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        // Evaluate routing for unstaffed department
        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/route")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStage").value("UNASSIGNED_TRIAGE"))
                .andExpect(jsonPath("$.responsibleRole").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.blockingReason", containsString("Routing Exception")))
                .andExpect(jsonPath("$.nextAction", containsString("Organization Administrator must assign")));
    }
}
