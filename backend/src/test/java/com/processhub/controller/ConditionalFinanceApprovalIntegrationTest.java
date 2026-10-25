package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.*;
import com.processhub.domain.enums.ExpenditureStatus;
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
import org.springframework.test.web.servlet.MvcResult;

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
class ConditionalFinanceApprovalIntegrationTest {

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
    private Department itDept;
    private IssueCategory hardwareCategory;
    private User technicianUser;
    private String technicianToken;
    private User financeApprover;
    private String financeToken;
    private User reporterUser;
    private String reporterToken;

    @BeforeEach
    void setUp() {
        cleanUpData();

        organization = organizationRepository.save(new Organization(UUID.randomUUID(), "Global Finance Corp", "GFC"));
        itDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "IT Operations", "ITO", "Enterprise IT"));

        hardwareCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, itDept, "SERVER_FAULT", "Server Hardware Fault",
                "Infrastructure", "BLADE", "HIGH", false));

        technicianUser = userRepository.save(new User(
                UUID.randomUUID(), "tech.dave@processhub.com", passwordEncoder.encode("Pass123!"),
                "Dave Hardware", "555-3333", Role.ROLE_TECHNICIAN, "IT"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, technicianUser, itDept, Role.ROLE_TECHNICIAN));
        technicianToken = jwtTokenProvider.generateToken(technicianUser);

        financeApprover = userRepository.save(new User(
                UUID.randomUUID(), "finance.sarah@processhub.com", passwordEncoder.encode("Pass123!"),
                "Sarah Finance", "555-4444", Role.ROLE_FINANCE_APPROVER, "Finance"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, financeApprover, itDept, Role.ROLE_FINANCE_APPROVER));
        financeToken = jwtTokenProvider.generateToken(financeApprover);

        reporterUser = userRepository.save(new User(
                UUID.randomUUID(), "rep.charlie@processhub.com", passwordEncoder.encode("Pass123!"),
                "Charlie Reporter", "555-5555", Role.ROLE_REPORTER, "Sales"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, reporterUser, itDept, Role.ROLE_REPORTER));
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

    private Issue createInProgressIssue() {
        Issue issue = new Issue(
                UUID.randomUUID(), "PH-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(),
                organization, itDept, hardwareCategory, reporterUser,
                "BLADE", "Server Fan Unit Failure", "Primary cooling fan failed on blade 4",
                "Data Center 1", "Rack 12", "SRV-409", Priority.HIGH
        );
        issue.setStatus(IssueStatus.IN_PROGRESS);
        issue.setAssignedTechnician(technicianUser);
        issue.setCurrentStage("IN_PROGRESS");
        issue.setResponsibleRole("ROLE_TECHNICIAN");
        return issueRepository.save(issue);
    }

    @Test
    @DisplayName("Expenditure request at or below threshold is auto-approved without blocking workflow")
    void testExpenditureBelowThresholdAutoApproved() throws Exception {
        Issue issue = createInProgressIssue();

        CreateExpenditureRequest req = new CreateExpenditureRequest(
                new BigDecimal("65.00"), "Replacement high-velocity fan module (OEM Part #FAN-44)"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/expenditures")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.currentStage").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.expenditures", hasSize(1)))
                .andExpect(jsonPath("$.expenditures[0].amount").value(65.00))
                .andExpect(jsonPath("$.expenditures[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.expenditures[0].decisionNotes").value(containsString("Auto-approved below threshold limit")))
                .andExpect(jsonPath("$.timeline[0].action").value("EXPENDITURE_APPROVED"))
                .andExpect(jsonPath("$.timeline[0].notes").value(containsString("Auto-approved expenditure of $65.00")));
    }

    @Test
    @DisplayName("Expenditure request exceeding threshold triggers AWAITING_FINANCE workflow state")
    void testExpenditureAboveThresholdBlocksIssue() throws Exception {
        Issue issue = createInProgressIssue();

        CreateExpenditureRequest req = new CreateExpenditureRequest(
                new BigDecimal("480.00"), "Replacement SAS backplane controller board"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/expenditures")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AWAITING_FINANCE"))
                .andExpect(jsonPath("$.currentStage").value("AWAITING_FINANCE"))
                .andExpect(jsonPath("$.responsibleRole").value("ROLE_FINANCE_APPROVER"))
                .andExpect(jsonPath("$.blockingReason").value(containsString("Awaiting finance authorization for expenditure of $480.00")))
                .andExpect(jsonPath("$.explainableProgress.blocked").value(true))
                .andExpect(jsonPath("$.expenditures", hasSize(1)))
                .andExpect(jsonPath("$.expenditures[0].amount").value(480.00))
                .andExpect(jsonPath("$.expenditures[0].status").value("PENDING"))
                .andExpect(jsonPath("$.timeline[0].action").value("EXPENDITURE_REQUESTED"));
    }

    @Test
    @DisplayName("Finance queue and metrics reflect pending spend requests and total value")
    void testFinanceQueueAndMetrics() throws Exception {
        Issue issue = createInProgressIssue();

        // Submit high-value expenditure
        CreateExpenditureRequest req = new CreateExpenditureRequest(
                new BigDecimal("500.00"), "High capacity power supply unit"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/expenditures")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Check Finance Queue
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/finance/queue")
                        .header("Authorization", "Bearer " + financeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(500.00))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].issueReferenceCode").value(issue.getReferenceCode()));

        // Check Finance Metrics
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/finance/metrics")
                        .header("Authorization", "Bearer " + financeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingApprovalCount").value(1))
                .andExpect(jsonPath("$.pendingTotalAmount").value(500.00));
    }

    @Test
    @DisplayName("Finance approval unblocks issue back to IN_PROGRESS for technician execution")
    void testFinanceApprovalResumesWork() throws Exception {
        Issue issue = createInProgressIssue();

        CreateExpenditureRequest req = new CreateExpenditureRequest(
                new BigDecimal("320.00"), "RAID cache backup battery"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/expenditures")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        IssueResponse issueRes = objectMapper.readValue(result.getResponse().getContentAsString(), IssueResponse.class);
        UUID expenditureId = issueRes.getExpenditures().get(0).getId();

        // Finance Approver decides APPROVED
        DecideExpenditureRequest decisionReq = new DecideExpenditureRequest(true, "Approved under IT Hardware Budget Code #9011");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/expenditures/" + expenditureId + "/decision")
                        .header("Authorization", "Bearer " + financeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decisionReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.currentStage").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.responsibleRole").value("ROLE_TECHNICIAN"))
                .andExpect(jsonPath("$.blockingReason").doesNotExist())
                .andExpect(jsonPath("$.explainableProgress.blocked").value(false))
                .andExpect(jsonPath("$.expenditures[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.expenditures[0].decisionNotes").value(containsString("Approved under IT Hardware Budget Code")))
                .andExpect(jsonPath("$.timeline[1].action").value("EXPENDITURE_APPROVED"));
    }

    @Test
    @DisplayName("Finance rejection routes issue back to UNDER_REVIEW supervisor queue with rationale")
    void testFinanceRejectionRoutesToSupervisor() throws Exception {
        Issue issue = createInProgressIssue();

        CreateExpenditureRequest req = new CreateExpenditureRequest(
                new BigDecimal("990.00"), "Full motherboard swap out-of-warranty"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/expenditures")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        IssueResponse issueRes = objectMapper.readValue(result.getResponse().getContentAsString(), IssueResponse.class);
        UUID expenditureId = issueRes.getExpenditures().get(0).getId();

        // Finance Approver decides REJECTED
        DecideExpenditureRequest decisionReq = new DecideExpenditureRequest(
                false, "Exceeds server residual asset value. Recommend retiring blade instead.");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/expenditures/" + expenditureId + "/decision")
                        .header("Authorization", "Bearer " + financeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decisionReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.currentStage").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$.responsibleRole").value("ROLE_SUPERVISOR"))
                .andExpect(jsonPath("$.blockingReason").value(containsString("Expenditure request for $990.00 was rejected by Finance")))
                .andExpect(jsonPath("$.expenditures[0].status").value("REJECTED"))
                .andExpect(jsonPath("$.timeline[1].action").value("EXPENDITURE_REJECTED"));
    }

    @Test
    @DisplayName("Reporter cannot decide expenditure authorization (RBAC check enforces 403 Forbidden)")
    void testReporterCannotDecideExpenditure() throws Exception {
        Issue issue = createInProgressIssue();

        CreateExpenditureRequest req = new CreateExpenditureRequest(
                new BigDecimal("300.00"), "Power cables and brackets"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/expenditures")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        IssueResponse issueRes = objectMapper.readValue(result.getResponse().getContentAsString(), IssueResponse.class);
        UUID expenditureId = issueRes.getExpenditures().get(0).getId();

        DecideExpenditureRequest decisionReq = new DecideExpenditureRequest(true, "Unauthorized reporter try");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues/" + issue.getId() + "/expenditures/" + expenditureId + "/decision")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(decisionReq)))
                .andExpect(status().isForbidden());
    }
}
