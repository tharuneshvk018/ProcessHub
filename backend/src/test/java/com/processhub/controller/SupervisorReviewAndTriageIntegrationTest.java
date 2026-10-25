package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.*;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SupervisorReviewAndTriageIntegrationTest {

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
    private IssueCategory leakCategory;
    private IssueCategory itCategory;
    private User facilitiesSupervisor;
    private String facSupervisorToken;
    private User reporterUser;
    private String reporterToken;
    private User otherReporter;
    private String otherReporterToken;
    private User technicianUser;

    @BeforeEach
    void setUp() {
        cleanUpData();

        organization = organizationRepository.save(new Organization(UUID.randomUUID(), "North Campus", "NORTH"));

        facilitiesDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "Facilities", "FAC", "Facilities"));
        itDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "IT Helpdesk", "IT", "Information Tech"));

        leakCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, facilitiesDept, "PIPE_LEAK", "Pipe Leak",
                "Leaks", "BURST", "HIGH", false));
        itCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, itDept, "NET_ISSUE", "Network Issue",
                "Network", "ROUTER", "MEDIUM", false));

        facilitiesSupervisor = userRepository.save(new User(
                UUID.randomUUID(), "fac.sup@processhub.com", passwordEncoder.encode("Pass123!"),
                "Marcus Aurelius", "555-8888", Role.ROLE_SUPERVISOR, "Facilities"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, facilitiesSupervisor, facilitiesDept, Role.ROLE_SUPERVISOR));
        facSupervisorToken = jwtTokenProvider.generateToken(facilitiesSupervisor);

        technicianUser = userRepository.save(new User(
                UUID.randomUUID(), "tech@processhub.com", passwordEncoder.encode("Pass123!"),
                "Bob Builder", "555-7777", Role.ROLE_TECHNICIAN, "Facilities"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, technicianUser, facilitiesDept, Role.ROLE_TECHNICIAN));

        reporterUser = userRepository.save(new User(
                UUID.randomUUID(), "reporter@processhub.com", passwordEncoder.encode("Pass123!"),
                "Alice Smith", "555-1234", Role.ROLE_REPORTER, "Operations"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, reporterUser, facilitiesDept, Role.ROLE_REPORTER));
        reporterToken = jwtTokenProvider.generateToken(reporterUser);

        otherReporter = userRepository.save(new User(
                UUID.randomUUID(), "other@processhub.com", passwordEncoder.encode("Pass123!"),
                "Eve Infiltrator", "555-0000", Role.ROLE_REPORTER, "Finance"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, otherReporter, itDept, Role.ROLE_REPORTER));
        otherReporterToken = jwtTokenProvider.generateToken(otherReporter);
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
    @DisplayName("Supervisor queue only returns issues from supervised department")
    void testSupervisorQueueDepartmentFiltering() throws Exception {
        // Facilities issue
        CreateIssueRequest facReq = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Burst pipe in basement parking", "Water spreading across parking bays 12 to 18.",
                "Parking Garage", "Level -1", null, "HIGH");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facReq)))
                .andExpect(status().isCreated());

        // IT issue
        CreateIssueRequest itReq = new CreateIssueRequest(
                itDept.getId(), itCategory.getId(), null,
                "Core network switch port dead", "Switch in server room 2 port 14 is completely dead.",
                "IT Building", "Server Rm 2", null, "MEDIUM");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + otherReporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itReq)))
                .andExpect(status().isCreated());

        // Facilities supervisor queries queue -> receives ONLY the Facilities issue
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/supervisor/queue")
                        .header("Authorization", "Bearer " + facSupervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Burst pipe in basement parking"))
                .andExpect(jsonPath("$[0].departmentName").value("Facilities"));
    }

    @Test
    @DisplayName("Non-supervisor (reporter) attempting to access supervisor queue is rejected (403 Forbidden)")
    void testReporterCannotAccessSupervisorQueue() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/supervisor/queue")
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Supervisor metrics correctly aggregate review, unassigned, and critical counts")
    void testSupervisorMetrics() throws Exception {
        CreateIssueRequest req1 = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Flooding in boiler room basement", "Major flooding endangering electrical sub-panel.",
                "Central Plant", "Boiler 1", null, "CRITICAL");

        CreateIssueRequest req2 = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Minor drip from bathroom faucet", "Slow drip in sink faucet 3rd floor.",
                "Admin Tower", "Restroom 3A", null, "LOW");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/supervisor/metrics")
                        .header("Authorization", "Bearer " + facSupervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingReviewCount").value(2))
                .andExpect(jsonPath("$.unassignedCount").value(2))
                .andExpect(jsonPath("$.criticalUrgentCount").value(1));
    }

    @Test
    @DisplayName("Supervisor starts review: status transitions to UNDER_REVIEW and logs REVIEW_STARTED audit event")
    void testStartReviewSuccess() throws Exception {
        CreateIssueRequest req = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Water leak in ceiling tiles", "Ceiling stained and dripping in room 204.",
                "Science Wing", "Room 204", null, "MEDIUM");

        MvcResult createResult = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Supervisor starts review
        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/review")
                        .header("Authorization", "Bearer " + facSupervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.currentStage").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.timeline", hasSize(2)))
                .andExpect(jsonPath("$.timeline[1].action").value("REVIEW_STARTED"))
                .andExpect(jsonPath("$.timeline[1].actorName").value("Marcus Aurelius"));
    }

    @Test
    @DisplayName("Rejection with mandatory reason closes report and records audit event")
    void testRejectIssueWithMandatoryReason() throws Exception {
        CreateIssueRequest req = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Request to install private espresso machine", "User requesting dedicated water line for personal machine.",
                "Admin Tower", "Room 401", null, "LOW");

        MvcResult createResult = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        RejectIssueRequest rejectReq = new RejectIssueRequest(
                "Personal appliances of this nature are strictly prohibited under facility policy section 4.2.");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/reject")
                        .header("Authorization", "Bearer " + facSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.currentStage").value("REJECTED"))
                .andExpect(jsonPath("$.blockingReason", containsString("Personal appliances")))
                .andExpect(jsonPath("$.closedAt").isNotEmpty())
                .andExpect(jsonPath("$.timeline[1].action").value("ISSUE_REJECTED"));
    }

    @Test
    @DisplayName("Rejection without reason is rejected by validation (400 Bad Request)")
    void testRejectWithoutReasonFails() throws Exception {
        CreateIssueRequest req = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Issue to be rejected without reason", "Description text here that is long enough.",
                "Admin Tower", "Room 401", null, "LOW");

        MvcResult createResult = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        RejectIssueRequest emptyReason = new RejectIssueRequest("   ");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/reject")
                        .header("Authorization", "Bearer " + facSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyReason)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Request for Information (RFI) dialogue and reporter response feedback loop")
    void testRfiDialogueAndReporterResponseLoop() throws Exception {
        CreateIssueRequest req = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Intermittent pipe knocking sound", "Banging noise occurs occasionally somewhere in the wall.",
                "East Wing", "Room 105", null, "MEDIUM");

        MvcResult createResult = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // 1. Supervisor requests more information (RFI)
        RfiRequest rfi = new RfiRequest("Does the knocking occur continuously or only when adjacent taps are activated?");
        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/request-info")
                        .header("Authorization", "Bearer " + facSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rfi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MORE_INFO_REQUESTED"))
                .andExpect(jsonPath("$.responsibleRole").value("ROLE_REPORTER"))
                .andExpect(jsonPath("$.blockingReason", containsString("knocking occur continuously")));

        // 2. Unrelated user attempts to answer RFI -> 403 Forbidden
        ProvideInfoRequest intruderResponse = new ProvideInfoRequest("Intruder attempting to answer RFI");
        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/provide-info")
                        .header("Authorization", "Bearer " + otherReporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(intruderResponse)))
                .andExpect(status().isForbidden());

        // 3. Original reporter answers RFI -> issue unblocked and transitions back to UNDER_REVIEW
        ProvideInfoRequest validResponse = new ProvideInfoRequest(
                "The knocking happens strictly when the autoclave in Lab 106 finishes a purge cycle.");
        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/provide-info")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validResponse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.responsibleRole").value("ROLE_SUPERVISOR"))
                .andExpect(jsonPath("$.blockingReason").isEmpty())
                .andExpect(jsonPath("$.description", containsString("autoclave in Lab 106")))
                .andExpect(jsonPath("$.timeline[2].action").value("MORE_INFO_PROVIDED"));
    }

    @Test
    @DisplayName("Supervisor approves report and assigns eligible department technician")
    void testApproveAndAssignTechnician() throws Exception {
        // Query eligible technicians
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/supervisor/departments/" + facilitiesDept.getId() + "/technicians")
                        .header("Authorization", "Bearer " + facSupervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName").value("Bob Builder"));

        CreateIssueRequest req = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Drinking fountain leaking continuously", "Fountain valve will not close shut.",
                "Gymnasium", "Concourse", null, "HIGH");

        MvcResult createResult = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Supervisor approves and assigns technician
        ApproveIssueRequest approveReq = new ApproveIssueRequest(
                technicianUser.getId(), "Bring replacement diaphragm seal kit.", "HIGH");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/approve")
                        .header("Authorization", "Bearer " + facSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedTechnicianName").value("Bob Builder"))
                .andExpect(jsonPath("$.responsibleRole").value("ROLE_TECHNICIAN"))
                .andExpect(jsonPath("$.nextAction", containsString("commence diagnosis and repair")))
                .andExpect(jsonPath("$.timeline[1].action").value("TECHNICIAN_ASSIGNED"))
                .andExpect(jsonPath("$.timeline[1].notes", containsString("diaphragm seal kit")));
    }

    @Test
    @DisplayName("Assigning non-technician user to issue is rejected (400 Bad Request)")
    void testAssignNonTechnicianRejected() throws Exception {
        CreateIssueRequest req = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Report to test invalid technician assignment", "Details here are long enough.",
                "Gymnasium", "Concourse", null, "HIGH");

        MvcResult createResult = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // Attempt to assign reporterUser as technician
        AssignTechnicianRequest invalidAssign = new AssignTechnicianRequest(reporterUser.getId(), "Invalid assignment");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/assign")
                        .header("Authorization", "Bearer " + facSupervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAssign)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not an active technician")));
    }

    @Test
    @DisplayName("Reporter attempting to approve issue is rejected (403 Forbidden)")
    void testReporterCannotApproveIssue() throws Exception {
        CreateIssueRequest req = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Issue to test unauthorized approval", "Details here are long enough.",
                "Gymnasium", "Concourse", null, "HIGH");

        MvcResult createResult = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        ApproveIssueRequest approveReq = new ApproveIssueRequest();

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId + "/approve")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isForbidden());
    }
}
