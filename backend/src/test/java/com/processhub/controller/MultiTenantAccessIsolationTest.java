package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.Department;
import com.processhub.domain.entity.Organization;
import com.processhub.domain.entity.OrganizationMembership;
import com.processhub.domain.entity.User;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.DepartmentRepository;
import com.processhub.domain.repository.IssueCategoryRepository;
import com.processhub.domain.repository.OrganizationMembershipRepository;
import com.processhub.domain.repository.OrganizationRepository;
import com.processhub.domain.repository.UserRepository;
import com.processhub.dto.CreateDepartmentRequest;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MultiTenantAccessIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private IssueCategoryRepository categoryRepository;

    @Autowired
    private OrganizationMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Organization orgAlpha;
    private Organization orgBeta;
    private User userAlpha;
    private String tokenAlpha;
    private User userBeta;
    private String tokenBeta;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        membershipRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();

        // Two completely distinct organizations
        orgAlpha = organizationRepository.save(new Organization(UUID.randomUUID(), "Campus Alpha", "CAMPUS-ALPHA"));
        orgBeta = organizationRepository.save(new Organization(UUID.randomUUID(), "Campus Beta", "CAMPUS-BETA"));

        // Departments for each
        departmentRepository.save(new Department(UUID.randomUUID(), orgAlpha, "Alpha Facilities", "FAC-A", "Desc"));
        departmentRepository.save(new Department(UUID.randomUUID(), orgBeta, "Beta Confidential Dept", "CONF-B", "Desc"));

        // User Alpha assigned strictly to Org Alpha
        userAlpha = userRepository.save(new User(
                UUID.randomUUID(),
                "alpha.user@processhub.com",
                passwordEncoder.encode("Pass123!"),
                "Alpha User",
                null,
                Role.ROLE_REPORTER,
                null
        ));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), orgAlpha, userAlpha, null, Role.ROLE_REPORTER));
        tokenAlpha = jwtTokenProvider.generateToken(userAlpha);

        // User Beta assigned strictly to Org Beta
        userBeta = userRepository.save(new User(
                UUID.randomUUID(),
                "beta.user@processhub.com",
                passwordEncoder.encode("Pass123!"),
                "Beta User",
                null,
                Role.ROLE_REPORTER,
                null
        ));
        membershipRepository.save(new OrganizationMembership(
                UUID.randomUUID(), orgBeta, userBeta, null, Role.ROLE_REPORTER));
        tokenBeta = jwtTokenProvider.generateToken(userBeta);
    }

    @Test
    @DisplayName("User Alpha can view departments belonging to their own organization (Campus Alpha)")
    void testUserCanAccessOwnOrganizationDepartments() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + orgAlpha.getId() + "/departments")
                        .header("Authorization", "Bearer " + tokenAlpha))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Alpha Facilities"));
    }

    @Test
    @DisplayName("User Alpha attempting to access Campus Beta departments via URL alteration is rejected (403 Forbidden)")
    void testUserCannotAccessOtherOrganizationDepartments() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + orgBeta.getId() + "/departments")
                        .header("Authorization", "Bearer " + tokenAlpha))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("not an authorized member")));
    }

    @Test
    @DisplayName("User Alpha attempting to access Campus Beta categories via URL alteration is rejected (403 Forbidden)")
    void testUserCannotAccessOtherOrganizationCategories() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + orgBeta.getId() + "/categories")
                        .header("Authorization", "Bearer " + tokenAlpha))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("User Alpha attempting to create department in Campus Beta is rejected (403 Forbidden)")
    void testUserCannotMutateOtherOrganizationData() throws Exception {
        CreateDepartmentRequest req = new CreateDepartmentRequest("Infiltrated Dept", "INFIL", "Tampered");

        mockMvc.perform(post("/api/v1/organizations/" + orgBeta.getId() + "/departments")
                        .header("Authorization", "Bearer " + tokenAlpha)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User Beta can view departments belonging to their own organization (Campus Beta)")
    void testUserBetaCanAccessOwnOrganizationDepartments() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/" + orgBeta.getId() + "/departments")
                        .header("Authorization", "Bearer " + tokenBeta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Beta Confidential Dept"));
    }
}
