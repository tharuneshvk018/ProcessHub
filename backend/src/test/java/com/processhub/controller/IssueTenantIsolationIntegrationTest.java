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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IssueTenantIsolationIntegrationTest {

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

    private Organization orgAlpha;
    private Organization orgBeta;
    private Department deptBeta;
    private IssueCategory catBeta;
    private User userAlpha;
    private String tokenAlpha;
    private User userBeta;
    private String tokenBeta;
    private Issue issueBeta;
    private IssueAttachment attachmentBeta;

    @BeforeEach
    void setUp() throws Exception {
        cleanUpData();

        orgAlpha = organizationRepository.save(new Organization(UUID.randomUUID(), "Alpha Organization", "ORG-ALPHA"));
        orgBeta = organizationRepository.save(new Organization(UUID.randomUUID(), "Beta Organization", "ORG-BETA"));

        Department deptAlpha = departmentRepository.save(new Department(
                UUID.randomUUID(), orgAlpha, "Alpha Facilities", "ALPHA-FAC", "Alpha facilities"));
        deptBeta = departmentRepository.save(new Department(
                UUID.randomUUID(), orgBeta, "Beta Confidential Operations", "BETA-OPS", "Beta ops"));

        categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), orgAlpha, deptAlpha, "ALPHA_MAINT", "Alpha Maintenance",
                "Alpha issues", "MAINT", "LOW", false));
        catBeta = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), orgBeta, deptBeta, "BETA_SECURITY", "Beta Security Fault",
                "Beta issues", "SECURITY", "CRITICAL", false));

        userAlpha = userRepository.save(new User(
                UUID.randomUUID(), "user.alpha@domain.com", passwordEncoder.encode("Pass123!"),
                "User Alpha", null, Role.ROLE_REPORTER, "Alpha Ops"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), orgAlpha, userAlpha, deptAlpha, Role.ROLE_REPORTER));
        tokenAlpha = jwtTokenProvider.generateToken(userAlpha);

        userBeta = userRepository.save(new User(
                UUID.randomUUID(), "user.beta@domain.com", passwordEncoder.encode("Pass123!"),
                "User Beta", null, Role.ROLE_REPORTER, "Beta Ops"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), orgBeta, userBeta, deptBeta, Role.ROLE_REPORTER));
        tokenBeta = jwtTokenProvider.generateToken(userBeta);

        // Beta user creates an issue in Org Beta
        CreateIssueRequest reqBeta = new CreateIssueRequest(
                deptBeta.getId(), catBeta.getId(), "SECURITY",
                "Confidential Beta security incident report", "Sensitive details of operational security issue in Org Beta.",
                "Beta Secure Wing", "Vault 1", null, "CRITICAL");

        MvcResult result = mockMvc.perform(post("/api/v1/organizations/" + orgBeta.getId() + "/issues")
                        .header("Authorization", "Bearer " + tokenBeta)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBeta)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID betaIssueId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
        issueBeta = issueRepository.findById(betaIssueId).orElseThrow();

        // Beta user uploads an attachment
        MockMultipartFile file = new MockMultipartFile(
                "file", "beta_confidential.png", "image/png", "confidential-beta-bytes".getBytes());

        MvcResult attResult = mockMvc.perform(multipart("/api/v1/organizations/" + orgBeta.getId() + "/issues/" + issueBeta.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + tokenBeta))
                .andExpect(status().isCreated())
                .andReturn();

        UUID attId = UUID.fromString(objectMapper.readTree(attResult.getResponse().getContentAsString()).get("id").asText());
        attachmentBeta = attachmentRepository.findById(attId).orElseThrow();
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
    @DisplayName("User Alpha cannot create issue in Organization Beta (403 Forbidden)")
    void testUserAlphaCannotCreateIssueInOrgBeta() throws Exception {
        CreateIssueRequest req = new CreateIssueRequest(
                deptBeta.getId(), catBeta.getId(), "SECURITY",
                "Tampered issue injection attempt", "Attempting to create report across tenant boundaries.",
                "Beta Bldg", "101", null, "HIGH");

        mockMvc.perform(post("/api/v1/organizations/" + orgBeta.getId() + "/issues")
                        .header("Authorization", "Bearer " + tokenAlpha)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User Alpha cannot list issues from Organization Beta (403 Forbidden)")
    void testUserAlphaCannotListOrgBetaIssues() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + orgBeta.getId() + "/issues")
                        .header("Authorization", "Bearer " + tokenAlpha))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User Alpha cannot view Organization Beta issue by ID (403 Forbidden)")
    void testUserAlphaCannotGetOrgBetaIssueById() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + orgBeta.getId() + "/issues/" + issueBeta.getId())
                        .header("Authorization", "Bearer " + tokenAlpha))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User Alpha cannot view Organization Beta issue by Reference Code in Org route (403 Forbidden)")
    void testUserAlphaCannotGetOrgBetaIssueByReference() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + orgBeta.getId() + "/issues/ref/" + issueBeta.getReferenceCode())
                        .header("Authorization", "Bearer " + tokenAlpha))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User Alpha cannot view Organization Beta issue via Global Direct Reference lookup (403 Forbidden)")
    void testUserAlphaCannotGetOrgBetaIssueDirectly() throws Exception {
        mockMvc.perform(get("/api/v1/issues/ref/" + issueBeta.getReferenceCode())
                        .header("Authorization", "Bearer " + tokenAlpha))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User Alpha cannot upload attachment to Organization Beta issue (403 Forbidden)")
    void testUserAlphaCannotUploadAttachmentToOrgBetaIssue() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "infiltrate.png", "image/png", "fake data".getBytes());

        mockMvc.perform(multipart("/api/v1/organizations/" + orgBeta.getId() + "/issues/" + issueBeta.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + tokenAlpha))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User Alpha cannot download attachment from Organization Beta issue (403 Forbidden)")
    void testUserAlphaCannotDownloadOrgBetaAttachment() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + orgBeta.getId() + "/issues/" + issueBeta.getId() + "/attachments/" + attachmentBeta.getId())
                        .header("Authorization", "Bearer " + tokenAlpha))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User Alpha cannot access Organization Beta reporter dashboard (403 Forbidden)")
    void testUserAlphaCannotAccessOrgBetaDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + orgBeta.getId() + "/issues/reporter/dashboard")
                        .header("Authorization", "Bearer " + tokenAlpha))
                .andExpect(status().isForbidden());
    }
}
