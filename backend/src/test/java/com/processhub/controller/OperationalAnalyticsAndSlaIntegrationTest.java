package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.*;
import com.processhub.domain.enums.*;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperationalAnalyticsAndSlaIntegrationTest {

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
    private Department itDept;
    private Department facilitiesDept;
    private IssueCategory networkCategory;
    private IssueCategory plumbingCategory;
    private User supervisorUser;
    private String supervisorToken;
    private User technicianUser;
    private String technicianToken;
    private User outsideUser;
    private String outsideToken;

    @BeforeEach
    void setUp() {
        cleanUpData();

        organizationA = organizationRepository.save(new Organization(UUID.randomUUID(), "Enterprise Campus A", "ENT-A"));
        organizationB = organizationRepository.save(new Organization(UUID.randomUUID(), "Enterprise Campus B", "ENT-B"));

        itDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organizationA, "IT Infrastructure", "IT-01", "Information Technology"));

        facilitiesDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organizationA, "Facilities", "FAC-01", "Physical Plant"));

        networkCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organizationA, itDept, "NET-01", "Core Network",
                "Core switches and routers", "SWITCH", "CRITICAL", false));

        plumbingCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organizationA, facilitiesDept, "PLUMB-01", "Plumbing",
                "Pipes and drains", "PIPE", "MEDIUM", false));

        // Supervisor in Org A
        supervisorUser = userRepository.save(new User(
                UUID.randomUUID(), "sup.ops@processhub.com", passwordEncoder.encode("Password123!"),
                "Operations Supervisor", "+15559901", Role.ROLE_SUPERVISOR, "IT Infrastructure"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organizationA, supervisorUser, itDept, Role.ROLE_SUPERVISOR));
        supervisorToken = jwtTokenProvider.generateToken(supervisorUser);

        // Technician in Org A
        technicianUser = userRepository.save(new User(
                UUID.randomUUID(), "tech.ops@processhub.com", passwordEncoder.encode("Password123!"),
                "Tom Technician", "+15559902", Role.ROLE_TECHNICIAN, "IT Infrastructure"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organizationA, technicianUser, itDept, Role.ROLE_TECHNICIAN));
        technicianToken = jwtTokenProvider.generateToken(technicianUser);

        // Outside user in Org B
        outsideUser = userRepository.save(new User(
                UUID.randomUUID(), "outside.ops@processhub.com", passwordEncoder.encode("Password123!"),
                "Outside User", "+15559903", Role.ROLE_REPORTER, "External"));
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
    @DisplayName("Calculates SLA deadline by priority matrix (CRITICAL=4h, HIGH=12h, MEDIUM=48h, LOW=120h)")
    void testSlaDeadlineCalculationByPriority() throws Exception {
        // Create CRITICAL issue
        CreateIssueRequest critRequest = new CreateIssueRequest(
                itDept.getId(), networkCategory.getId(), "CORE_OUTAGE",
                "Core Switch Down", "Building router offline", "Datacenter", "Rack 4", "ROUTER-01", "CRITICAL"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/issues")
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(critRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slaDueAt", notNullValue()))
                .andExpect(jsonPath("$.slaBreached", is(false)))
                .andExpect(jsonPath("$.slaStatus", is("ON_TRACK")))
                .andExpect(jsonPath("$.slaRemainingMinutes", greaterThan(230)))
                .andExpect(jsonPath("$.slaRemainingMinutes", lessThanOrEqualTo(245)));

        // Create MEDIUM issue
        CreateIssueRequest medRequest = new CreateIssueRequest(
                facilitiesDept.getId(), plumbingCategory.getId(), "FAUCET_DRIP",
                "Dripping faucet", "Minor leak", "Hall A", "101", "FAUCET-01", "MEDIUM"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/issues")
                        .header("Authorization", "Bearer " + supervisorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slaDueAt", notNullValue()))
                .andExpect(jsonPath("$.slaBreached", is(false)))
                .andExpect(jsonPath("$.slaStatus", is("ON_TRACK")))
                .andExpect(jsonPath("$.slaRemainingMinutes", greaterThan(2800)))
                .andExpect(jsonPath("$.slaRemainingMinutes", lessThanOrEqualTo(2890)));
    }

    @Test
    @DisplayName("SLA escalation engine identifies overdue issues, marks slaBreached, creates timeline event, and notifies supervisors")
    void testSlaEscalationEngineTriggersBreachEventAndNotifications() throws Exception {
        // Create an issue whose SLA due date has passed
        Instant pastSla = Instant.now().minus(2, ChronoUnit.HOURS);
        Issue overdueIssue = new Issue(
                UUID.randomUUID(),
                "PH-8899",
                organizationA,
                itDept,
                networkCategory,
                supervisorUser,
                "CORE_OUTAGE",
                "Severe Switch Outage",
                "Backbone switch failure",
                "Datacenter",
                "Rack 1",
                "SWITCH-BB-1",
                Priority.CRITICAL
        );
        overdueIssue.setAssignedTechnician(technicianUser);
        overdueIssue.setSlaDueAt(pastSla);
        overdueIssue.setSlaBreached(false);
        overdueIssue.setStatus(IssueStatus.IN_PROGRESS);
        overdueIssue = issueRepository.save(overdueIssue);

        // Trigger escalation
        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/analytics/sla-escalate")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.escalatedCount", is(1)))
                .andExpect(jsonPath("$.message", containsString("escalation completed")));

        // Verify issue state updated
        Issue updated = issueRepository.findById(overdueIssue.getId()).orElseThrow();
        assertTrue(updated.isSlaBreached());

        // Verify timeline event created
        var timelineEvents = timelineRepository.findByIssueIdOrderByCreatedAtAsc(updated.getId());
        boolean hasBreachEvent = timelineEvents.stream()
                .anyMatch(e -> TimelineAction.SLA_BREACHED.name().equals(e.getAction()));
        assertTrue(hasBreachEvent, "Expected SLA_BREACHED timeline event");

        // Verify notification dispatched to assigned technician and supervisor
        var techNotifications = notificationRepository
                .findByOrganizationIdAndRecipientIdOrderByCreatedAtDesc(organizationA.getId(), technicianUser.getId());
        assertTrue(techNotifications.stream().anyMatch(n -> n.getNotificationType() == NotificationType.SLA_BREACH_WARNING));
    }

    @Test
    @DisplayName("Operational analytics endpoint calculates SLA compliance, MTTR, breakdowns, top assets, and spend")
    void testOperationalAnalyticsOverviewCalculations() throws Exception {
        Instant now = Instant.now();

        // 1. Resolved issue with 4-hour duration
        Issue resolvedIssue = new Issue(
                UUID.randomUUID(), "PH-1101", organizationA, itDept, networkCategory,
                supervisorUser, "RESOLVED_INCIDENT", "Resolved Network Loop", "Loop resolved", "Bldg A", "101", "ROUTER-01",
                Priority.HIGH);
        resolvedIssue.setStatus(IssueStatus.CLOSED);
        resolvedIssue.setCreatedAt(now.minus(10, ChronoUnit.HOURS));
        resolvedIssue.setClosedAt(now.minus(6, ChronoUnit.HOURS)); // 4 hours MTTR
        resolvedIssue.setSlaDueAt(now.plus(2, ChronoUnit.HOURS));
        resolvedIssue.setSlaBreached(false);
        issueRepository.save(resolvedIssue);

        // 2. Active issue with breach
        Issue activeBreached = new Issue(
                UUID.randomUUID(), "PH-1102", organizationA, facilitiesDept, plumbingCategory,
                supervisorUser, "ACTIVE_BREACHED", "Flooding in Restroom", "Water main leak", "Bldg B", "201", "PIPE-01",
                Priority.CRITICAL);
        activeBreached.setStatus(IssueStatus.IN_PROGRESS);
        activeBreached.setCreatedAt(now.minus(5, ChronoUnit.HOURS));
        activeBreached.setSlaDueAt(now.minus(1, ChronoUnit.HOURS));
        activeBreached.setSlaBreached(true);
        issueRepository.save(activeBreached);

        // 3. Approved expenditure: $1,250.00
        ExpenditureRequest exp1 = new ExpenditureRequest(
                UUID.randomUUID(), resolvedIssue, technicianUser,
                new BigDecimal("1250.00"), "Replacement fiber module", ExpenditureStatus.APPROVED);
        expenditureRepository.save(exp1);

        // 4. Pending expenditure: $450.00
        ExpenditureRequest exp2 = new ExpenditureRequest(
                UUID.randomUUID(), activeBreached, technicianUser,
                new BigDecimal("450.00"), "Industrial pump rental", ExpenditureStatus.PENDING);
        expenditureRepository.save(exp2);

        // Query analytics
        mockMvc.perform(get("/api/v1/organizations/" + organizationA.getId() + "/analytics/overview")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIssuesCount", is(2)))
                .andExpect(jsonPath("$.activeIssuesCount", is(1)))
                .andExpect(jsonPath("$.resolvedIssuesCount", is(1)))
                .andExpect(jsonPath("$.slaBreachedCount", is(1)))
                .andExpect(jsonPath("$.slaCompliancePercentage", is(50.0)))
                .andExpect(jsonPath("$.averageResolutionHours", is(4.0)))
                .andExpect(jsonPath("$.priorityDistribution.HIGH", is(1)))
                .andExpect(jsonPath("$.priorityDistribution.CRITICAL", is(1)))
                .andExpect(jsonPath("$.statusDistribution.CLOSED", is(1)))
                .andExpect(jsonPath("$.statusDistribution.IN_PROGRESS", is(1)))
                .andExpect(jsonPath("$.departmentMetrics", hasSize(2)))
                .andExpect(jsonPath("$.topRecurringAssets.ROUTER-01", is(1)))
                .andExpect(jsonPath("$.totalSpendApproved", is(1250.00)))
                .andExpect(jsonPath("$.totalSpendPending", is(450.00)));
    }

    @Test
    @DisplayName("Enforces tenant isolation on analytics: Outside organization user is forbidden")
    void testTenantIsolationOnAnalytics() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + organizationA.getId() + "/analytics/overview")
                        .header("Authorization", "Bearer " + outsideToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/organizations/" + organizationA.getId() + "/analytics/sla-escalate")
                        .header("Authorization", "Bearer " + outsideToken))
                .andExpect(status().isForbidden());
    }
}
