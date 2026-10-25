package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.*;
import com.processhub.domain.enums.*;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EndToEndWorkflowScenariosIntegrationTest {

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
    private Department itDept;
    private Department emptyDept;
    private IssueCategory plumbingCategory;
    private IssueCategory networkCategory;
    private IssueCategory unstaffedCategory;

    private User reporterAlice;
    private String reporterToken;

    private User facilitiesSupervisorMarcus;
    private String facilitiesSupervisorToken;

    private User itSupervisorSarah;
    private String itSupervisorToken;

    private User technicianBob;
    private String technicianToken;

    private User financeFiona;
    private String financeToken;

    private User adminArthur;
    private String adminToken;

    @BeforeEach
    void setUp() {
        cleanUpData();

        // 1. Organization
        organization = organizationRepository.save(new Organization(UUID.randomUUID(), "ProcessHub Enterprise HQ", "HQ"));

        // 2. Departments
        facilitiesDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "Facilities & Maintenance", "FAC", "Campus facilities and civil maintenance"));
        itDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "IT Services", "IT", "Enterprise computing and network operations"));
        emptyDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "Unstaffed Operations", "UNSTAFFED", "Department with no supervisors"));

        // 3. Categories
        plumbingCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, facilitiesDept, "WATER_LEAK", "Plumbing & Leakage",
                "Plumbing", "VALVE", "HIGH", false));
        networkCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, itDept, "NETWORK_OUTAGE", "Network Connectivity Outage",
                "Network", "ROUTER", "CRITICAL", false));
        unstaffedCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, emptyDept, "CIVIL_DEFECT", "Structural Civil Defect",
                "Civil", "WALL", "MEDIUM", false));

        // 4. Users & Memberships
        reporterAlice = userRepository.save(new User(
                UUID.randomUUID(), "alice.reporter@processhub.com", passwordEncoder.encode("Pass123!"),
                "Alice Reporter", "555-0100", Role.ROLE_REPORTER, "Operations"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, reporterAlice, facilitiesDept, Role.ROLE_REPORTER));
        reporterToken = jwtTokenProvider.generateToken(reporterAlice);

        facilitiesSupervisorMarcus = userRepository.save(new User(
                UUID.randomUUID(), "marcus.supervisor@processhub.com", passwordEncoder.encode("Pass123!"),
                "Marcus Aurelius", "555-0101", Role.ROLE_SUPERVISOR, "Facilities"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, facilitiesSupervisorMarcus, facilitiesDept, Role.ROLE_SUPERVISOR));
        facilitiesSupervisorToken = jwtTokenProvider.generateToken(facilitiesSupervisorMarcus);

        itSupervisorSarah = userRepository.save(new User(
                UUID.randomUUID(), "sarah.it@processhub.com", passwordEncoder.encode("Pass123!"),
                "Sarah Connor", "555-0102", Role.ROLE_SUPERVISOR, "IT"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, itSupervisorSarah, itDept, Role.ROLE_SUPERVISOR));
        itSupervisorToken = jwtTokenProvider.generateToken(itSupervisorSarah);

        technicianBob = userRepository.save(new User(
                UUID.randomUUID(), "bob.tech@processhub.com", passwordEncoder.encode("Pass123!"),
                "Bob Builder", "555-0103", Role.ROLE_TECHNICIAN, "Facilities"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, technicianBob, facilitiesDept, Role.ROLE_TECHNICIAN));
        technicianToken = jwtTokenProvider.generateToken(technicianBob);

        financeFiona = userRepository.save(new User(
                UUID.randomUUID(), "fiona.finance@processhub.com", passwordEncoder.encode("Pass123!"),
                "Fiona Finance", "555-0104", Role.ROLE_FINANCE_APPROVER, "Finance"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, financeFiona, null, Role.ROLE_FINANCE_APPROVER));
        financeToken = jwtTokenProvider.generateToken(financeFiona);

        adminArthur = userRepository.save(new User(
                UUID.randomUUID(), "arthur.admin@processhub.com", passwordEncoder.encode("Pass123!"),
                "Arthur Admin", "555-0105", Role.ROLE_ADMIN, "Executive"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, adminArthur, null, Role.ROLE_ADMIN));
        adminToken = jwtTokenProvider.generateToken(adminArthur);
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

    // =========================================================================
    // Scenario 1: Direct maintenance repair without financial approval
    // =========================================================================
    @Test
    @DisplayName("Scenario 1: Full lifecycle of direct maintenance repair with spend under threshold")
    void testScenario1_DirectMaintenanceRepairWithoutFinanceApproval() throws Exception {
        // Step 1: Alice reports water leak
        CreateIssueRequest createReq = new CreateIssueRequest();
        createReq.setDepartmentId(facilitiesDept.getId());
        createReq.setCategoryId(plumbingCategory.getId());
        createReq.setSubtype("PIPE_BURST");
        createReq.setTitle("Ceiling Water Leak in Science Wing");
        createReq.setDescription("Water dripping through acoustic tile above lab bench 4");
        createReq.setLocationBuilding("Science Block A");
        createReq.setLocationRoom("Lab 102");
        createReq.setAssetId("PIPE-SCI-102");
        createReq.setPriority(Priority.HIGH.name());

        String createRespStr = mockMvc.perform(post("/api/v1/organizations/{orgId}/issues", organization.getId())
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referenceCode", startsWith("PH-")))
                .andExpect(jsonPath("$.status", is("SUBMITTED")))
                .andReturn().getResponse().getContentAsString();

        UUID issueId = UUID.fromString(objectMapper.readTree(createRespStr).get("id").asText());

        // Step 2: Route issue to supervisor queue
        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/route", organization.getId(), issueId)
                        .header("Authorization", "Bearer " + facilitiesSupervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.explainableProgress.currentStage", is("UNDER_SUPERVISOR_REVIEW")));

        // Step 3: Marcus starts review and assigns Bob
        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/review", organization.getId(), issueId)
                        .header("Authorization", "Bearer " + facilitiesSupervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UNDER_REVIEW")));

        AssignTechnicianRequest assignReq = new AssignTechnicianRequest();
        assignReq.setTechnicianId(technicianBob.getId());
        assignReq.setInstructions("Priority leak, dispatch immediately with pipe clamps.");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/assign", organization.getId(), issueId)
                        .header("Authorization", "Bearer " + facilitiesSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ASSIGNED")))
                .andExpect(jsonPath("$.assignedTechnicianName", is("Bob Builder")));

        // Step 4: Bob starts work
        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/start", organization.getId(), issueId)
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        // Step 5: Bob logs progress and requests small expenditure ($45 <= $100 auto-approved)
        UpdateProgressRequest progressReq = new UpdateProgressRequest();
        progressReq.setNotes("Isolated shutoff valve, replacing section of copper pipe");
        progressReq.setProgressPercentage(60);

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/progress", organization.getId(), issueId)
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progressReq)))
                .andExpect(status().isOk());

        CreateExpenditureRequest spendReq = new CreateExpenditureRequest();
        spendReq.setAmount(new BigDecimal("45.00"));
        spendReq.setJustification("Copper pipe segment and two compression fittings");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/expenditures", organization.getId(), issueId)
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spendReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS"))); // Remains IN_PROGRESS because auto-approved!

        // Step 6: Bob submits resolution
        SubmitResolutionRequest resolveReq = new SubmitResolutionRequest();
        resolveReq.setWorkSummary("Replaced corroded copper pipe with new PEX coupling and tested pressure.");
        resolveReq.setRootCause("Localized galvanic corrosion at junction");
        resolveReq.setLaborHours(new BigDecimal("2.5"));
        resolveReq.setPartsUsed("1m PEX pipe, 2x 1/2in compression couplers");
        resolveReq.setResolutionEvidenceUrl("https://storage.processhub.com/evidence/leak-fixed.jpg");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/resolve", organization.getId(), issueId)
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED_PENDING_VERIFICATION")))
                .andExpect(jsonPath("$.explainableProgress.responsibleRole", is("ROLE_SUPERVISOR")));

        // Step 7: Marcus independently verifies and closes issue
        VerifyResolutionRequest verifyReq = new VerifyResolutionRequest();
        verifyReq.setApproved(true);
        verifyReq.setVerificationNotes("Site inspected. Ceiling tile replaced, no drips under full line pressure.");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/verify", organization.getId(), issueId)
                        .header("Authorization", "Bearer " + facilitiesSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CLOSED")))
                .andExpect(jsonPath("$.closedAt", notNullValue()));
    }

    // =========================================================================
    // Scenario 2: Maintenance repair requiring financial approval
    // =========================================================================
    @Test
    @DisplayName("Scenario 2: Repair requiring conditional finance authorization exceeding threshold")
    void testScenario2_MaintenanceRepairRequiringFinanceApproval() throws Exception {
        // Pre-create issue in IN_PROGRESS state assigned to Bob
        Issue issue = createInProgressIssue();

        // Step 1: Bob requests expenditure > $100.00 ($380.00 for pump motor)
        CreateExpenditureRequest spendReq = new CreateExpenditureRequest();
        spendReq.setAmount(new BigDecimal("380.00"));
        spendReq.setJustification("Commercial sump pump motor burned out, requires OEM motor unit replacement");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/expenditures", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spendReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("AWAITING_FINANCE")))
                .andExpect(jsonPath("$.explainableProgress.responsibleRole", is("ROLE_FINANCE_APPROVER")))
                .andExpect(jsonPath("$.explainableProgress.blockingReason", containsString("380.00")));

        // Step 2: Fiona inspects Finance Queue
        String queueRespStr = mockMvc.perform(get("/api/v1/organizations/{orgId}/finance/queue", organization.getId())
                        .header("Authorization", "Bearer " + financeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].amount", is(380.00)))
                .andReturn().getResponse().getContentAsString();

        UUID expenditureId = UUID.fromString(objectMapper.readTree(queueRespStr).get(0).get("id").asText());

        // Step 3: Fiona approves expenditure
        DecideExpenditureRequest decideReq = new DecideExpenditureRequest();
        decideReq.setApproved(true);
        decideReq.setNotes("Approved against Facilities Q4 Contingency Budget Code #FAC-409");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/expenditures/{expId}/decision",
                        organization.getId(), issue.getId(), expenditureId)
                        .header("Authorization", "Bearer " + financeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decideReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.explainableProgress.blockingReason", nullValue()));

        // Step 4: Bob completes work and submits resolution
        SubmitResolutionRequest resolveReq = new SubmitResolutionRequest();
        resolveReq.setWorkSummary("Installed replacement motor, recalibrated float switch");
        resolveReq.setRootCause("Winding burnout due to sediment blockage");
        resolveReq.setLaborHours(new BigDecimal("3.0"));
        resolveReq.setPartsUsed("OEM Motor #MTR-400");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/resolve", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED_PENDING_VERIFICATION")));

        // Step 5: Reporter Alice verifies and closes
        VerifyResolutionRequest verifyReq = new VerifyResolutionRequest();
        verifyReq.setApproved(true);
        verifyReq.setVerificationNotes("Pump running silently, water drained from sump basin.");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/verify", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CLOSED")));
    }

    // =========================================================================
    // Scenario 3: Rejected report with a recorded reason
    // =========================================================================
    @Test
    @DisplayName("Scenario 3: Supervisor rejects out-of-scope report with mandatory reason")
    void testScenario3_RejectedReportWithRecordedReason() throws Exception {
        Issue issue = createSubmittedIssue("Personal kettle not heating up", "Kitchenette");

        // Reject with empty reason fails validation
        RejectIssueRequest emptyReasonReq = new RejectIssueRequest();
        emptyReasonReq.setReason("");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/reject", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + facilitiesSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyReasonReq)))
                .andExpect(status().isBadRequest());

        // Reject with valid reason
        RejectIssueRequest validRejectReq = new RejectIssueRequest();
        validRejectReq.setReason("Personal consumer appliances are not serviced by Facilities Maintenance.");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/reject", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + facilitiesSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.closedAt", notNullValue()))
                .andExpect(jsonPath("$.explainableProgress.currentStage", is("REJECTED")));

        // Verify timeline contains ISSUE_REJECTED
        mockMvc.perform(get("/api/v1/organizations/{orgId}/issues/{id}", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeline", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.timeline[?(@.action == 'ISSUE_REJECTED')].notes",
                        hasItem(containsString("Personal consumer appliances"))));
    }

    // =========================================================================
    // Scenario 4: Request for information (RFI) and subsequent resubmission
    // =========================================================================
    @Test
    @DisplayName("Scenario 4: Request for information (RFI) loop and reporter resubmission")
    void testScenario4_RequestForInformationAndResubmission() throws Exception {
        Issue issue = createSubmittedIssue("Flickering overhead light in corridor", "Main Corridor");

        // Step 1: Marcus issues RFI
        RfiRequest rfiReq = new RfiRequest();
        rfiReq.setQuestion("Please specify the nearest door number or pillar tag to identify the fixture.");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/request-info", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + facilitiesSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rfiReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("MORE_INFO_REQUESTED")))
                .andExpect(jsonPath("$.explainableProgress.responsibleRole", is("ROLE_REPORTER")))
                .andExpect(jsonPath("$.explainableProgress.nextAction", containsString("must respond with requested details")));

        // Step 2: Unauthorized user attempts to answer RFI (should fail 403)
        ProvideInfoRequest infoReq = new ProvideInfoRequest();
        infoReq.setResponse("Next to Room 204");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/provide-info", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + itSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(infoReq)))
                .andExpect(status().isForbidden());

        // Step 3: Alice responds to RFI
        infoReq.setResponse("Light fixture directly opposite Room 204 near water dispenser.");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/provide-info", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(infoReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UNDER_REVIEW")))
                .andExpect(jsonPath("$.explainableProgress.responsibleRole", is("ROLE_SUPERVISOR")));
    }

    // =========================================================================
    // Scenario 5: Correct routing between IT and Facilities
    // =========================================================================
    @Test
    @DisplayName("Scenario 5: Multi-factor routing dispatches issues accurately between IT and Facilities")
    void testScenario5_CorrectRoutingBetweenItAndFacilities() throws Exception {
        // Report 1: Facilities problem
        CreateIssueRequest facReq = new CreateIssueRequest();
        facReq.setDepartmentId(facilitiesDept.getId());
        facReq.setCategoryId(plumbingCategory.getId());
        facReq.setSubtype("PIPE_BURST");
        facReq.setTitle("Restroom pipe valve failure");
        facReq.setDescription("Restroom water pressure valve whistling loudly");
        facReq.setLocationBuilding("Main Complex");
        facReq.setLocationRoom("Restroom 1F");
        facReq.setPriority(Priority.HIGH.name());

        String facRespStr = mockMvc.perform(post("/api/v1/organizations/{orgId}/issues", organization.getId())
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID facIssueId = UUID.fromString(objectMapper.readTree(facRespStr).get("id").asText());

        // Report 2: IT problem
        CreateIssueRequest itReq = new CreateIssueRequest();
        itReq.setDepartmentId(itDept.getId());
        itReq.setCategoryId(networkCategory.getId());
        itReq.setSubtype("CORE_SWITCH");
        itReq.setTitle("Server Room Switch Port Down");
        itReq.setDescription("Core rack switch port 24 failing intermittent packet drops");
        itReq.setLocationBuilding("Data Center");
        itReq.setLocationRoom("Server Room B");
        itReq.setPriority(Priority.CRITICAL.name());

        String itRespStr = mockMvc.perform(post("/api/v1/organizations/{orgId}/issues", organization.getId())
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID itIssueId = UUID.fromString(objectMapper.readTree(itRespStr).get("id").asText());

        // Route both issues so they transition to UNDER_REVIEW
        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/route", organization.getId(), facIssueId)
                        .header("Authorization", "Bearer " + facilitiesSupervisorToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/route", organization.getId(), itIssueId)
                        .header("Authorization", "Bearer " + itSupervisorToken))
                .andExpect(status().isOk());

        // Facilities supervisor Marcus sees facIssue in queue, but NOT itIssue
        mockMvc.perform(get("/api/v1/organizations/{orgId}/supervisor/queue", organization.getId())
                        .header("Authorization", "Bearer " + facilitiesSupervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(facIssueId.toString())))
                .andExpect(jsonPath("$[*].id", not(hasItem(itIssueId.toString()))));

        // IT supervisor Sarah sees itIssue in queue, but NOT facIssue
        mockMvc.perform(get("/api/v1/organizations/{orgId}/supervisor/queue", organization.getId())
                        .header("Authorization", "Bearer " + itSupervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(itIssueId.toString())))
                .andExpect(jsonPath("$[*].id", not(hasItem(facIssueId.toString()))));
    }

    // =========================================================================
    // Scenario 6: Unauthorized approval attempt
    // =========================================================================
    @Test
    @DisplayName("Scenario 6: Role guard prevents unauthorized approval/rejection attempts")
    void testScenario6_UnauthorizedApprovalAttempt() throws Exception {
        Issue issue = createSubmittedIssue("Broken door handle", "East Wing");

        // Reporter Alice tries to approve issue (Forbidden)
        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/approve", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isForbidden());

        // Technician Bob tries to reject issue (Forbidden)
        RejectIssueRequest rejectReq = new RejectIssueRequest();
        rejectReq.setReason("Not my job to reject this report");
        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/reject", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isForbidden());

        // IT Supervisor Sarah tries to assign technician in Facilities department (Forbidden)
        AssignTechnicianRequest assignReq = new AssignTechnicianRequest();
        assignReq.setTechnicianId(technicianBob.getId());
        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/assign", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + itSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignReq)))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // Scenario 7: Missing reviewer configuration (Unassigned triage)
    // =========================================================================
    @Test
    @DisplayName("Scenario 7: Unstaffed department report safely falls back to unassigned triage")
    void testScenario7_MissingReviewerConfigurationUnassignedTriage() throws Exception {
        CreateIssueRequest unstaffedReq = new CreateIssueRequest();
        unstaffedReq.setDepartmentId(emptyDept.getId());
        unstaffedReq.setCategoryId(unstaffedCategory.getId());
        unstaffedReq.setSubtype("WALL_CRACK");
        unstaffedReq.setTitle("Cracked foundation wall");
        unstaffedReq.setDescription("Visible horizontal fissure on north external foundation");
        unstaffedReq.setLocationBuilding("Annex C");
        unstaffedReq.setLocationRoom("Basement");
        unstaffedReq.setPriority(Priority.MEDIUM.name());

        String createRespStr = mockMvc.perform(post("/api/v1/organizations/{orgId}/issues", organization.getId())
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unstaffedReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID issueId = UUID.fromString(objectMapper.readTree(createRespStr).get("id").asText());

        // Route evaluates unstaffed department and triggers exception handling
        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/route", organization.getId(), issueId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStage", is("UNASSIGNED_TRIAGE")))
                .andExpect(jsonPath("$.responsibleRole", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$.blockingReason", containsString("Routing Exception")));
    }

    // =========================================================================
    // Scenario 8: Work completed but awaiting verification (Separation of duties)
    // =========================================================================
    @Test
    @DisplayName("Scenario 8: Separation of duties strictly blocks technician self-verification")
    void testScenario8_SeparationOfDutiesEnforcement() throws Exception {
        Issue issue = createInProgressIssue();

        // Technician Bob submits resolution
        SubmitResolutionRequest resolveReq = new SubmitResolutionRequest();
        resolveReq.setWorkSummary("Repaired circuit breaker contactor");
        resolveReq.setLaborHours(new BigDecimal("1.5"));

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/resolve", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("RESOLVED_PENDING_VERIFICATION")));

        // Bob attempts to verify his own resolution -> 403 Forbidden!
        VerifyResolutionRequest verifyReq = new VerifyResolutionRequest();
        verifyReq.setApproved(true);
        verifyReq.setVerificationNotes("I certify my own work was done well");

        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/verify", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Separation of duties")));

        // Independent supervisor Marcus verifies -> 200 OK
        verifyReq.setVerificationNotes("Inspected contactor and tested load cycle. Approved.");
        mockMvc.perform(post("/api/v1/organizations/{orgId}/issues/{id}/verify", organization.getId(), issue.getId())
                        .header("Authorization", "Bearer " + facilitiesSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CLOSED")));
    }

    // =========================================================================
    // Scenario 9: Missed deadline and escalation
    // =========================================================================
    @Test
    @DisplayName("Scenario 9: SLA escalation engine identifies overdue issues and marks SLA breached")
    void testScenario9_MissedDeadlineAndSlaEscalation() throws Exception {
        // Create an overdue issue with slaDueAt set in the past
        Issue overdueIssue = new Issue(
                UUID.randomUUID(), "PH-SLA-9001", organization, facilitiesDept, plumbingCategory, reporterAlice,
                "PIPE", "Overdue HVAC Leak", "Water pooling in mechanical room",
                "Building 4", "Mech Room", "HVAC-01", Priority.CRITICAL
        );
        overdueIssue.setStatus(IssueStatus.UNDER_REVIEW);
        overdueIssue.setCurrentStage("UNDER_SUPERVISOR_REVIEW");
        overdueIssue.setResponsibleRole("ROLE_SUPERVISOR");
        overdueIssue.setSlaDueAt(Instant.now().minus(2, ChronoUnit.HOURS)); // 2 hours overdue!
        overdueIssue.setSlaBreached(false);
        overdueIssue = issueRepository.save(overdueIssue);

        // Trigger SLA escalation scan
        mockMvc.perform(post("/api/v1/organizations/{orgId}/analytics/sla-escalate", organization.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escalatedCount", greaterThanOrEqualTo(1)));

        // Verify issue record updated
        mockMvc.perform(get("/api/v1/organizations/{orgId}/issues/{id}", organization.getId(), overdueIssue.getId())
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slaBreached", is(true)))
                .andExpect(jsonPath("$.slaStatus", is("BREACHED")));
    }

    // =========================================================================
    // Scenario 10: Recurring issue with accessible historical records
    // =========================================================================
    @Test
    @DisplayName("Scenario 10: Recurring issue intelligence flags repeat asset failures with history")
    void testScenario10_RecurringIssueIntelligenceWithHistoricalRecords() throws Exception {
        String assetId = "CHILLER-CH-400";

        // Seed 3 historical closed incidents on this asset
        for (int i = 1; i <= 3; i++) {
            Issue hist = new Issue(
                    UUID.randomUUID(), "PH-HIST-" + i, organization, facilitiesDept, plumbingCategory, reporterAlice,
                    "CHILLER", "Compressor vibration failure #" + i, "Bearing noise observed",
                    "Plant Room", "B1", assetId, Priority.HIGH
            );
            hist.setStatus(IssueStatus.CLOSED);
            hist.setClosedAt(Instant.now().minus(i * 10, ChronoUnit.DAYS));
            issueRepository.save(hist);
        }

        // Alice reports 4th issue on same asset
        CreateIssueRequest recurReq = new CreateIssueRequest();
        recurReq.setDepartmentId(facilitiesDept.getId());
        recurReq.setCategoryId(plumbingCategory.getId());
        recurReq.setSubtype("CHILLER");
        recurReq.setTitle("Chiller compressor tripping again");
        recurReq.setDescription("Chiller thermal overload relay tripped for the 4th time");
        recurReq.setLocationBuilding("Plant Room");
        recurReq.setLocationRoom("B1");
        recurReq.setAssetId(assetId);
        recurReq.setPriority(Priority.CRITICAL.name());

        String createRespStr = mockMvc.perform(post("/api/v1/organizations/{orgId}/issues", organization.getId())
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recurReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID currentIssueId = UUID.fromString(objectMapper.readTree(createRespStr).get("id").asText());

        // Query recurrence endpoint
        mockMvc.perform(get("/api/v1/organizations/{orgId}/issues/{id}/recurrence", organization.getId(), currentIssueId)
                        .header("Authorization", "Bearer " + facilitiesSupervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recurrenceLevel", is("CHRONIC_REPEAT")))
                .andExpect(jsonPath("$.chronic", is(true)))
                .andExpect(jsonPath("$.totalRecurrences", is(3)))
                .andExpect(jsonPath("$.recommendation", containsString("Chronic repeat failure")))
                .andExpect(jsonPath("$.relatedIssues", hasSize(3)));
    }

    // Helper methods
    private Issue createSubmittedIssue(String title, String room) {
        Issue issue = new Issue(
                UUID.randomUUID(), "PH-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(),
                organization, facilitiesDept, plumbingCategory, reporterAlice,
                "GENERAL", title, "Description for " + title,
                "North Building", room, null, Priority.MEDIUM
        );
        issue.setStatus(IssueStatus.UNDER_REVIEW);
        issue.setCurrentStage("UNDER_SUPERVISOR_REVIEW");
        issue.setResponsibleRole("ROLE_SUPERVISOR");
        return issueRepository.save(issue);
    }

    private Issue createInProgressIssue() {
        Issue issue = new Issue(
                UUID.randomUUID(), "PH-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(),
                organization, facilitiesDept, plumbingCategory, reporterAlice,
                "PUMP", "Sump Pump Malfunction", "Sump pump basin overflowing in basement",
                "Utility Block", "Sub-Basement", "PUMP-409", Priority.HIGH
        );
        issue.setStatus(IssueStatus.IN_PROGRESS);
        issue.setAssignedTechnician(technicianBob);
        issue.setCurrentStage("IN_PROGRESS");
        issue.setResponsibleRole("ROLE_TECHNICIAN");
        return issueRepository.save(issue);
    }
}
