package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.Organization;
import com.processhub.domain.entity.User;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.DepartmentRepository;
import com.processhub.domain.repository.OrganizationMembershipRepository;
import com.processhub.domain.repository.OrganizationRepository;
import com.processhub.domain.repository.UserRepository;
import com.processhub.dto.CreateDepartmentRequest;
import com.processhub.dto.CreateOrganizationRequest;
import com.processhub.security.JwtTokenProvider;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrganizationManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private String adminToken;
    private User regularUser;
    private String userToken;

    @BeforeEach
    void setUp() {
        departmentRepository.deleteAll();
        membershipRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = new User(
                UUID.randomUUID(),
                "admin@processhub.com",
                passwordEncoder.encode("AdminPass123!"),
                "Admin Officer",
                null,
                Role.ROLE_ADMIN,
                "ADMINISTRATION"
        );
        userRepository.save(adminUser);
        adminToken = jwtTokenProvider.generateToken(adminUser);

        regularUser = new User(
                UUID.randomUUID(),
                "reporter@processhub.com",
                passwordEncoder.encode("ReporterPass123!"),
                "Field Reporter",
                null,
                Role.ROLE_REPORTER,
                null
        );
        userRepository.save(regularUser);
        userToken = jwtTokenProvider.generateToken(regularUser);
    }

    @Test
    @DisplayName("Admin can create new organization")
    void testAdminCreateOrganization() throws Exception {
        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "Central Campus Operations",
                "CAMPUS-MAIN"
        );

        mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Central Campus Operations"))
                .andExpect(jsonPath("$.code").value("CAMPUS-MAIN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Non-admin user cannot create organization (403 Forbidden)")
    void testNonAdminCannotCreateOrganization() throws Exception {
        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "Unauthorized Org",
                "UNAUTH-ORG"
        );

        mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Creating organization with duplicate code fails (409 Conflict)")
    void testDuplicateOrganizationCodeFails() throws Exception {
        Organization existing = new Organization(UUID.randomUUID(), "Existing Org", "DUP-CODE");
        organizationRepository.save(existing);

        CreateOrganizationRequest request = new CreateOrganizationRequest("Second Org", "DUP-CODE");

        mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Admin can create departments within an organization")
    void testCreateDepartmentsInOrganization() throws Exception {
        Organization org = organizationRepository.save(new Organization(UUID.randomUUID(), "Metropolitan Operations", "METRO-OPS"));

        CreateDepartmentRequest req1 = new CreateDepartmentRequest("Facilities and Maintenance", "FACILITIES", "Building structures and HVAC");
        CreateDepartmentRequest req2 = new CreateDepartmentRequest("Electrical Maintenance", "ELECTRICAL", "High voltage and power distribution");
        CreateDepartmentRequest req3 = new CreateDepartmentRequest("Plumbing Services", "PLUMBING", "Water piping and fixtures");

        mockMvc.perform(post("/api/v1/organizations/" + org.getId() + "/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("FACILITIES"));

        mockMvc.perform(post("/api/v1/organizations/" + org.getId() + "/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ELECTRICAL"));

        mockMvc.perform(post("/api/v1/organizations/" + org.getId() + "/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("PLUMBING"));

        // List departments
        mockMvc.perform(get("/api/v1/organizations/" + org.getId() + "/departments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].code", containsInAnyOrder("FACILITIES", "ELECTRICAL", "PLUMBING")));
    }

    @Test
    @DisplayName("Duplicate department code in same organization fails (409 Conflict)")
    void testDuplicateDepartmentCodeInSameOrgFails() throws Exception {
        Organization org = organizationRepository.save(new Organization(UUID.randomUUID(), "Tech Park", "TECH-PARK"));
        CreateDepartmentRequest req = new CreateDepartmentRequest("IT Support", "IT", "Core IT network and services");

        mockMvc.perform(post("/api/v1/organizations/" + org.getId() + "/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/organizations/" + org.getId() + "/departments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
