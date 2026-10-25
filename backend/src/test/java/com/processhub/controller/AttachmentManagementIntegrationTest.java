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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttachmentManagementIntegrationTest {

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
    private User reporterUser;
    private String reporterToken;
    private Issue testIssue;

    @BeforeEach
    void setUp() throws Exception {
        cleanUpData();

        organization = organizationRepository.save(new Organization(UUID.randomUUID(), "Tech Center", "TECH-CTR"));
        department = departmentRepository.save(new Department(
                UUID.randomUUID(), organization, "Facilities", "FAC-TECH", "Facilities maintenance"));
        category = categoryRepository.save(new IssueCategory(
                UUID.randomUUID(), organization, department, "ELECTRICAL", "Electrical Outage",
                "Power trips", "TRIP,BREAKER", "HIGH", false));

        reporterUser = userRepository.save(new User(
                UUID.randomUUID(), "att.reporter@processhub.com", passwordEncoder.encode("Secret123!"),
                "Alex Rivera", "555-4321", Role.ROLE_REPORTER, "Operations"));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), organization, reporterUser, department, Role.ROLE_REPORTER));
        reporterToken = jwtTokenProvider.generateToken(reporterUser);

        CreateIssueRequest req = new CreateIssueRequest(
                department.getId(), category.getId(), "TRIP",
                "Circuit breaker tripped on 3rd floor", "Power lost across rooms 301-305. Refrigerator units offline.",
                "Tech Center Tower", "Floor 3", null, "HIGH");

        MvcResult result = mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/issues")
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID issueId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
        testIssue = issueRepository.findById(issueId).orElseThrow();
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
    @DisplayName("Upload valid PNG image attachment returns 201 Created and records timeline event")
    void testUploadValidImageAttachment() throws Exception {
        byte[] content = "fake-png-content-bytes".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "breaker_photo.png", "image/png", content);

        mockMvc.perform(multipart("/api/v1/organizations/" + organization.getId() + "/issues/" + testIssue.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fileName").value("breaker_photo.png"))
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.fileSize").value(content.length))
                .andExpect(jsonPath("$.uploadedByName").value("Alex Rivera"))
                .andExpect(jsonPath("$.downloadUrl", containsString("/attachments/")));

        // Verify attachment event recorded in issue timeline
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/issues/" + testIssue.getId())
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attachments", hasSize(1)))
                .andExpect(jsonPath("$.timeline", hasSize(2))) // ISSUE_SUBMITTED + ATTACHMENT_ADDED
                .andExpect(jsonPath("$.timeline[1].action").value("ATTACHMENT_ADDED"))
                .andExpect(jsonPath("$.timeline[1].notes", containsString("breaker_photo.png")));
    }

    @Test
    @DisplayName("Upload valid PDF document attachment returns 201 Created")
    void testUploadValidPdfAttachment() throws Exception {
        byte[] pdfBytes = "%PDF-1.4 sample pdf content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "incident_log.pdf", "application/pdf", pdfBytes);

        mockMvc.perform(multipart("/api/v1/organizations/" + organization.getId() + "/issues/" + testIssue.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("incident_log.pdf"))
                .andExpect(jsonPath("$.contentType").value("application/pdf"));
    }

    @Test
    @DisplayName("Authenticated streaming download returns 200 OK with proper headers and exact file content")
    void testDownloadAttachmentStream() throws Exception {
        byte[] expectedBytes = "photographic evidence data bytes".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "evidence.jpg", "image/jpeg", expectedBytes);

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/organizations/" + organization.getId() + "/issues/" + testIssue.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isCreated())
                .andReturn();

        String attachmentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult downloadResult = mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/issues/" + testIssue.getId() + "/attachments/" + attachmentId)
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"evidence.jpg\"")))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andReturn();

        byte[] actualBytes = downloadResult.getResponse().getContentAsByteArray();
        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    @DisplayName("Uploading executable file is rejected (400 Bad Request)")
    void testUploadExecutableRejected() throws Exception {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file", "payload.exe", "application/x-msdownload", "executable binary".getBytes());

        mockMvc.perform(multipart("/api/v1/organizations/" + organization.getId() + "/issues/" + testIssue.getId() + "/attachments")
                        .file(exeFile)
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not permitted")));
    }

    @Test
    @DisplayName("Uploading script file is rejected (400 Bad Request)")
    void testUploadScriptRejected() throws Exception {
        MockMultipartFile scriptFile = new MockMultipartFile(
                "file", "script.sh", "application/x-sh", "#!/bin/bash\necho hello".getBytes());

        mockMvc.perform(multipart("/api/v1/organizations/" + organization.getId() + "/issues/" + testIssue.getId() + "/attachments")
                        .file(scriptFile)
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("not permitted")));
    }

    @Test
    @DisplayName("Uploading empty file is rejected (400 Bad Request)")
    void testUploadEmptyFileRejected() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/api/v1/organizations/" + organization.getId() + "/issues/" + testIssue.getId() + "/attachments")
                        .file(emptyFile)
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("empty")));
    }

    @Test
    @DisplayName("Requesting non-existent attachment returns 404 Not Found")
    void testDownloadNonExistentAttachment() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/issues/" + testIssue.getId() + "/attachments/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + reporterToken))
                .andExpect(status().isNotFound());
    }
}
