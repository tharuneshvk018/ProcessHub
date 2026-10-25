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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IssueReportingIntegrationTest {

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
    private Department electricalDept;
    private IssueCategory leakCategory;
    private IssueCategory equipmentCategory;
    private User reporterUser;
    private String reporterToken;

    @BeforeEach
    void setUp() {
        cleanUpData();

        organization = organizationRepository.save(new Organization(UUID.randomUUID(), "Main Campus", "MAIN-CAMPUS"));

        facilitiesDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "Facilities & Maintenance", "FAC-01", "Facilities management"));
        electricalDept = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "Electrical Support", "ELEC-01", "Electrical maintenance"));

        leakCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, facilitiesDept, "WATER_LEAK", "Water Leakage",
                "Pipe burst, sink overflow", "PIPE_BURST,FAUCET_LEAK", "HIGH", false));

        equipmentCategory = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, facilitiesDept, "LAB_EQUIPMENT", "Lab Equipment Fault",
                "Fume hoods, autoclaves", "CALIBRATION,HEATING", "MEDIUM", true));

        reporterUser = userRepository.save(new User(
                UUID.randomUUID(),
                "reporter@processhub.com",
                passwordEncoder.encode("Password123!"),
                "Sarah Connor",
                "555-0199",
                Role.ROLE_REPORTER,
                "Operations"
        ));

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
    @DisplayName("Successfully submit problem report, generating unique PH-XXXX reference, initial status, and explainable progress")
    void testCreateIssueSuccess() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                facilitiesDept.getId(),
                leakCategory.getId(),
                "PIPE_BURST",
                "Severe pipe burst in Science Wing corridor",
                "Water is pooling rapidly near Lab Room 302. Slipping hazard and electrical risk.",
                "Science Wing",
                "Room 302",
                null,
                "HIGH"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.referenceCode", matchesPattern("^PH-\\d{4,}$")))
                .andExpect(jsonPath("$.title").value("Severe pipe burst in Science Wing corridor"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.statusDisplayName").value("Submitted"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.reporterName").value("Sarah Connor"))
                .andExpect(jsonPath("$.explainableProgress.currentStage").value("SUBMITTED"))
                .andExpect(jsonPath("$.explainableProgress.responsibleRole").value("ROLE_SUPERVISOR"))
                .andExpect(jsonPath("$.explainableProgress.nextAction", containsString("Awaiting supervisor triage")))
                .andExpect(jsonPath("$.timeline", hasSize(1)))
                .andExpect(jsonPath("$.timeline[0].action").value("ISSUE_SUBMITTED"));
    }

    @Test
    @DisplayName("Sequential issue creation produces distinct sequential PH-XXXX references")
    void testSequentialIssuesGenerateUniqueReferences() throws Exception {
        CreateIssueRequest req1 = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "First issue title report", "Detailed description for the first issue report.",
                "Building A", "101", null, "MEDIUM");

        CreateIssueRequest req2 = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Second distinct issue report", "Detailed description for the second issue report.",
                "Building B", "202", null, "MEDIUM");

        MvcResult res1 = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult res2 = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andReturn();

        String ref1 = objectMapper.readTree(res1.getResponse().getContentAsString()).get("referenceCode").asText();
        String ref2 = objectMapper.readTree(res2.getResponse().getContentAsString()).get("referenceCode").asText();

        assertNotEquals(ref1, ref2);
    }

    @Test
    @DisplayName("Category requiring asset rejects submission when asset ID is omitted (400 Bad Request)")
    void testAssetMandatoryForCategory() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                facilitiesDept.getId(),
                equipmentCategory.getId(),
                "CALIBRATION",
                "Centrifuge spinning unevenly with severe vibration",
                "Centrifuge rotor vibrating violently upon reaching 4000 RPM. Requires technician inspection.",
                "Research Complex",
                "Lab B-12",
                null, // Missing mandatory asset ID
                "HIGH"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Asset identifier is mandatory")));
    }

    @Test
    @DisplayName("Category requiring asset succeeds when asset ID is provided")
    void testAssetSuppliedForMandatoryCategory() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                facilitiesDept.getId(),
                equipmentCategory.getId(),
                "CALIBRATION",
                "Centrifuge spinning unevenly with severe vibration",
                "Centrifuge rotor vibrating violently upon reaching 4000 RPM. Requires technician inspection.",
                "Research Complex",
                "Lab B-12",
                "EQUIP-CENT-88",
                "HIGH"
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assetId").value("EQUIP-CENT-88"));
    }

    @Test
    @DisplayName("Input validation: blank title rejected (400 Bad Request)")
    void testValidationBlankTitle() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "    ", "Description text must be long enough.", "Bldg A", "101", null, "LOW");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Input validation: short title (< 5 chars) rejected (400 Bad Request)")
    void testValidationShortTitle() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Leak", "Description text must be long enough.", "Bldg A", "101", null, "LOW");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Input validation: short description (< 10 chars) rejected (400 Bad Request)")
    void testValidationShortDescription() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Valid Title Here", "Too short", "Bldg A", "101", null, "LOW");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Input validation: missing department ID rejected (400 Bad Request)")
    void testValidationMissingDepartment() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                null, leakCategory.getId(), null,
                "Valid Title Here", "Valid description here that is long enough", "Bldg A", "101", null, "LOW");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Input validation: missing location rejected (400 Bad Request)")
    void testValidationMissingLocation() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Valid Title Here", "Valid description here that is long enough", "", "", null, "LOW");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Cross-department category mismatch rejected (400 Bad Request)")
    void testCategoryDepartmentMismatchRejected() throws Exception {
        // leakCategory belongs to facilitiesDept, but electricalDept is supplied
        CreateIssueRequest request = new CreateIssueRequest(
                electricalDept.getId(), leakCategory.getId(), null,
                "Valid Title Here", "Valid description here that is long enough", "Bldg A", "101", null, "LOW");

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("does not belong to department")));
    }

    @Test
    @DisplayName("Rapid duplicate submission detected and rejected (409 Conflict)")
    void testDuplicateSubmissionPrevention() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), "PIPE_BURST",
                "Identical Water Leak Detected", "Water flooding the floor rapidly right now.",
                "East Wing", "Hallway", null, "HIGH");

        // First submission succeeds
        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Repeated click / identical submission within 30 seconds is rejected
        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Duplicate submission detected")));
    }

    @Test
    @DisplayName("Retrieve issue by ID and by reference code (PH-XXXX)")
    void testGetIssueByIdAndReference() throws Exception {
        CreateIssueRequest request = new CreateIssueRequest(
                facilitiesDept.getId(), leakCategory.getId(), null,
                "Sink broken in bathroom", "Continuous overflowing sink in 2nd floor bathroom.",
                "Administration Block", "Restroom 2B", null, "MEDIUM");

        MvcResult createResult = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String issueId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();
        String refCode = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("referenceCode").asText();

        // Retrieve by ID
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/issues/" + issueId)
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId))
                .andExpect(jsonPath("$.referenceCode").value(refCode));

        // Retrieve by Org Reference Code
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/issues/ref/" + refCode)
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId));

        // Retrieve by Global Direct Reference Code
        mockMvc.perform(get("/api/v1/issues/ref/" + refCode)
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId));
    }
}
