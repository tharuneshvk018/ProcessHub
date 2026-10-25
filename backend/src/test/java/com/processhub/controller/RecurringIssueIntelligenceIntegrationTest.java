package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.*;
import com.processhub.domain.enums.IssueStatus;
import com.processhub.domain.enums.Priority;
import com.processhub.domain.enums.RecurrenceLevel;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecurringIssueIntelligenceIntegrationTest {

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
    private Department hvacDept;
    private IssueCategory hvacCategory;
    private User reporterUser;
    private String reporterToken;
    private User outsideUser;
    private String outsideToken;

    @BeforeEach
    void setUp() {
        cleanUpData();

        organizationA = organizationRepository.save(new Organization(UUID.randomUUID(), "Corporate Facility Org", "CORP-01"));
        organizationB = organizationRepository.save(new Organization(UUID.randomUUID(), "Separate Facility Org", "SEP-01"));

        hvacDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organizationA, "HVAC Operations", "HVAC-01", "Climate control"));

        hvacCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organizationA, hvacDept, "CHILL-01", "Chiller Systems",
                "Chiller equipment", "COMPRESSOR", "HIGH", false));

        reporterUser = userRepository.save(new User(
                UUID.randomUUID(), "reporter.intel@processhub.com", passwordEncoder.encode("Password123!"),
                "Intel Reporter", "+15551111", Role.ROLE_REPORTER, "HVAC Operations"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organizationA, reporterUser, hvacDept, Role.ROLE_REPORTER));
        reporterToken = jwtTokenProvider.generateToken(reporterUser);

        outsideUser = userRepository.save(new User(
                UUID.randomUUID(), "outside.intel@processhub.com", passwordEncoder.encode("Password123!"),
                "Outside Tenant", "+15552222", Role.ROLE_REPORTER, "External"));
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

    private Issue seedHistoricalIssue(Organization org, String assetId, String building, String room, Instant createdAt) {
        Issue issue = new Issue(
                UUID.randomUUID(),
                "PH-" + (1000 + (int)(Math.random() * 8000)),
                org,
                hvacDept,
                hvacCategory,
                reporterUser,
                "COMPRESSOR_TRIP",
                "Compressor failure on " + assetId,
                "Compressor overheated and tripped breaker",
                building,
                room,
                assetId,
                Priority.HIGH
        );
        issue.setCreatedAt(createdAt);
        issue.setStatus(IssueStatus.CLOSED);
        issue.setClosedAt(createdAt.plus(2, ChronoUnit.HOURS));
        issue.setWorkSummary("Replaced thermal breaker and reset compressor.");
        return issueRepository.save(issue);
    }

    @Test
    @DisplayName("Returns recurrence level NONE and chronic false when no prior incidents exist within 90 days")
    void testRecurrenceNoneWhenNoPriorIncidents() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                hvacDept.getId(),
                hvacCategory.getId(),
                "CHILLER_LEAK",
                "Chiller Refrigerant Leak",
                "Refrigerant leak detected on unit",
                "North Plant",
                "Mechanical Room 1",
                "CHILLER-ALPHA-01",
                "HIGH"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurrenceIntelligence.totalRecurrences", is(0)))
                .andExpect(jsonPath("$.recurrenceIntelligence.recurrenceLevel", is(RecurrenceLevel.NONE.name())))
                .andExpect(jsonPath("$.recurrenceIntelligence.chronic", is(false)))
                .andExpect(jsonPath("$.recurrenceIntelligence.relatedIssues", hasSize(0)));
    }

    @Test
    @DisplayName("Returns recurrence level LOW when 1 prior incident on same asset exists within 90 days")
    void testRecurrenceLowWhenOnePriorIncidentOnAsset() throws Exception {
        seedHistoricalIssue(organizationA, "CHILLER-ALPHA-01", "North Plant", "Mechanical Room 1",
                Instant.now().minus(15, ChronoUnit.DAYS));

        CreateIssueRequest request = new CreateIssueRequest(
                hvacDept.getId(),
                hvacCategory.getId(),
                "CHILLER_LEAK",
                "Chiller Refrigerant Leak Again",
                "Second leak noticed around seals",
                "North Plant",
                "Mechanical Room 1",
                "CHILLER-ALPHA-01",
                "HIGH"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurrenceIntelligence.totalRecurrences", is(1)))
                .andExpect(jsonPath("$.recurrenceIntelligence.assetMatchCount", is(1)))
                .andExpect(jsonPath("$.recurrenceIntelligence.recurrenceLevel", is(RecurrenceLevel.LOW.name())))
                .andExpect(jsonPath("$.recurrenceIntelligence.chronic", is(false)))
                .andExpect(jsonPath("$.recurrenceIntelligence.relatedIssues", hasSize(1)));
    }

    @Test
    @DisplayName("Returns recurrence level ELEVATED with chronic flag true when 2 prior incidents exist")
    void testRecurrenceElevatedWhenTwoPriorIncidents() throws Exception {
        seedHistoricalIssue(organizationA, "CHILLER-BETA-02", "North Plant", "Mechanical Room 1",
                Instant.now().minus(40, ChronoUnit.DAYS));
        seedHistoricalIssue(organizationA, "CHILLER-BETA-02", "North Plant", "Mechanical Room 1",
                Instant.now().minus(10, ChronoUnit.DAYS));

        CreateIssueRequest request = new CreateIssueRequest(
                hvacDept.getId(),
                hvacCategory.getId(),
                "CHILLER_TRIP",
                "Chiller Beta tripped offline",
                "Repeated trip under nominal load",
                "North Plant",
                "Mechanical Room 1",
                "CHILLER-BETA-02",
                "HIGH"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurrenceIntelligence.totalRecurrences", is(2)))
                .andExpect(jsonPath("$.recurrenceIntelligence.recurrenceLevel", is(RecurrenceLevel.ELEVATED.name())))
                .andExpect(jsonPath("$.recurrenceIntelligence.chronic", is(true)))
                .andExpect(jsonPath("$.recurrenceIntelligence.recommendation", containsString("Elevated recurrence")));
    }

    @Test
    @DisplayName("Returns recurrence level CHRONIC_REPEAT with replacement overhaul recommendation when 3+ prior incidents occur")
    void testRecurrenceChronicRepeatWhenThreeOrMorePriorIncidents() throws Exception {
        seedHistoricalIssue(organizationA, "CHILLER-GAMMA-03", "Central Plant", "Floor 1", Instant.now().minus(60, ChronoUnit.DAYS));
        seedHistoricalIssue(organizationA, "CHILLER-GAMMA-03", "Central Plant", "Floor 1", Instant.now().minus(35, ChronoUnit.DAYS));
        seedHistoricalIssue(organizationA, "CHILLER-GAMMA-03", "Central Plant", "Floor 1", Instant.now().minus(5, ChronoUnit.DAYS));

        CreateIssueRequest request = new CreateIssueRequest(
                hvacDept.getId(),
                hvacCategory.getId(),
                "CHILLER_FAILURE",
                "Catastrophic Bearing Noise",
                "Unit emitting violent screeching sound",
                "Central Plant",
                "Floor 1",
                "CHILLER-GAMMA-03",
                "CRITICAL"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurrenceIntelligence.totalRecurrences", is(3)))
                .andExpect(jsonPath("$.recurrenceIntelligence.recurrenceLevel", is(RecurrenceLevel.CHRONIC_REPEAT.name())))
                .andExpect(jsonPath("$.recurrenceIntelligence.chronic", is(true)))
                .andExpect(jsonPath("$.recurrenceIntelligence.recommendation", containsString("replacement or structural overhaul")));
    }

    @Test
    @DisplayName("Correlates recurring issues by location building and room when asset ID is omitted")
    void testLocationCorrelationWhenAssetIdOmitted() throws Exception {
        seedHistoricalIssue(organizationA, null, "Building 4", "Room 401", Instant.now().minus(20, ChronoUnit.DAYS));

        CreateIssueRequest request = new CreateIssueRequest(
                hvacDept.getId(),
                hvacCategory.getId(),
                "AIR_HANDLER_ISSUE",
                "Air Handler Fan Fault",
                "Air handler squealing loudly",
                "Building 4",
                "Room 401",
                null, // No asset ID
                "MEDIUM"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurrenceIntelligence.totalRecurrences", is(1)))
                .andExpect(jsonPath("$.recurrenceIntelligence.locationMatchCount", is(1)))
                .andExpect(jsonPath("$.recurrenceIntelligence.recurrenceLevel", is(RecurrenceLevel.LOW.name())));
    }

    @Test
    @DisplayName("Ignores incidents older than 90-day rolling window")
    void testOldIncidentsOutside90DaysIgnored() throws Exception {
        // Incident created 100 days ago
        seedHistoricalIssue(organizationA, "CHILLER-OLD-99", "Plant East", "Room 10", Instant.now().minus(100, ChronoUnit.DAYS));

        CreateIssueRequest request = new CreateIssueRequest(
                hvacDept.getId(),
                hvacCategory.getId(),
                "CHILLER_ISSUE",
                "Fresh Incident on Old Chiller",
                "First problem after 100 days of stable run",
                "Plant East",
                "Room 10",
                "CHILLER-OLD-99",
                "MEDIUM"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurrenceIntelligence.totalRecurrences", is(0)))
                .andExpect(jsonPath("$.recurrenceIntelligence.recurrenceLevel", is(RecurrenceLevel.NONE.name())));
    }

    @Test
    @DisplayName("Enforces tenant isolation: Never correlates asset recurrence across different organizations")
    void testCrossTenantIsolationNoRecurrenceLeakage() throws Exception {
        // Issue on same asset name in Org B
        seedHistoricalIssue(organizationB, "SHARED-ASSET-CODE-100", "Bldg B", "Room 1", Instant.now().minus(10, ChronoUnit.DAYS));

        CreateIssueRequest request = new CreateIssueRequest(
                hvacDept.getId(),
                hvacCategory.getId(),
                "CHILLER_ISSUE",
                "Org A Incident with same asset tag",
                "Issue in Org A",
                "Bldg A",
                "Room 1",
                "SHARED-ASSET-CODE-100",
                "MEDIUM"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurrenceIntelligence.totalRecurrences", is(0)))
                .andExpect(jsonPath("$.recurrenceIntelligence.recurrenceLevel", is(RecurrenceLevel.NONE.name())));
    }

    @Test
    @DisplayName("Queries dedicated recurrence endpoint directly")
    void testGetIssueRecurrenceDirectEndpoint() throws Exception {
        Issue issue = seedHistoricalIssue(organizationA, "ASSET-DIRECT-1", "Bldg Direct", "Room 100", Instant.now().minus(1, ChronoUnit.DAYS));

        mockMvc.perform(get("/api/v1/organizations/" + organizationA.getId() + "/issues/" + issue.getId() + "/recurrence")
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recurrenceLevel", notNullValue()))
                .andExpect(jsonPath("$.recommendation", notNullValue()));
    }
}
