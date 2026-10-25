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

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReporterDashboardIntegrationTest {

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
    private Department department;
    private IssueCategory category;
    private User reporter1;
    private String token1;
    private User reporter2;
    private String token2;

    @BeforeEach
    void setUp() {
        cleanUpData();

        organization = organizationRepository.save(new Organization(UUID.randomUUID(), "West Campus", "WEST-CAMPUS"));
        department = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "IT Services", "IT-DEPT", "IT network support"));
        category = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, department, "NETWORK", "Network Outage",
                "Wi-Fi drops, switch down", "WIFI,ETHERNET", "HIGH", false));

        reporter1 = userRepository.save(new User(
                UUID.randomUUID(), "reporter1@processhub.com", passwordEncoder.encode("Pass123!"),
                "Reporter One", "555-1111", Role.ROLE_REPORTER, "IT"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, reporter1, department, Role.ROLE_REPORTER));
        token1 = jwtTokenProvider.generateToken(reporter1);

        reporter2 = userRepository.save(new User(
                UUID.randomUUID(), "reporter2@processhub.com", passwordEncoder.encode("Pass123!"),
                "Reporter Two", "555-2222", Role.ROLE_REPORTER, "IT"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, reporter2, department, Role.ROLE_REPORTER));
        token2 = jwtTokenProvider.generateToken(reporter2);
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
    @DisplayName("My Reports returns only reports filed by authenticated reporter")
    void testMyReportsIsolation() throws Exception {
        // Reporter 1 submits 2 reports
        CreateIssueRequest req1 = new CreateIssueRequest(department.getId(), category.getId(), null,
                "Report One from User 1", "Wi-Fi not connecting in library 1st floor", "Library", "Floor 1", null, "HIGH");
        CreateIssueRequest req2 = new CreateIssueRequest(department.getId(), category.getId(), null,
                "Report Two from User 1", "Printer network offline in staff room", "Library", "Staff Rm", null, "LOW");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        // Reporter 2 submits 1 report
        CreateIssueRequest req3 = new CreateIssueRequest(department.getId(), category.getId(), null,
                "Report from User 2", "Projector HDMI cable broken", "Hall A", "Main", null, "MEDIUM");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isCreated());

        // Reporter 1 queries /my-reports -> receives exactly 2
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/issues/my-reports")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].reporterEmail").value("reporter1@processhub.com"))
                .andExpect(jsonPath("$[1].reporterEmail").value("reporter1@processhub.com"));

        // Reporter 2 queries /my-reports -> receives exactly 1
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/issues/my-reports")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Report from User 2"));
    }

    @Test
    @DisplayName("Reporter dashboard computes accurate summary statistics and recent reports")
    void testReporterDashboardSummary() throws Exception {
        // Reporter 1 submits 2 reports
        CreateIssueRequest req1 = new CreateIssueRequest(department.getId(), category.getId(), null,
                "Active Problem Report", "Active network connectivity outage in admin office", "Admin Bldg", "101", null, "HIGH");
        CreateIssueRequest req2 = new CreateIssueRequest(department.getId(), category.getId(), null,
                "Second Problem Report", "Slow ethernet speeds across the department", "Admin Bldg", "102", null, "MEDIUM");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/issues/reporter/dashboard")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReported").value(2))
                .andExpect(jsonPath("$.activeIssues").value(2))
                .andExpect(jsonPath("$.resolvedIssues").value(0))
                .andExpect(jsonPath("$.actionRequiredCount").value(0))
                .andExpect(jsonPath("$.recentReports", hasSize(2)));
    }

    @Test
    @DisplayName("Unauthenticated request to reporter dashboard returns 401 Unauthorized")
    void testUnauthenticatedDashboardFails() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/issues/reporter/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}
