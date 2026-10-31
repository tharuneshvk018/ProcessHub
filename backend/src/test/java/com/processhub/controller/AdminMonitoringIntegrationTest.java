package com.processhub.controller;

import com.processhub.domain.entity.*;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.*;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminMonitoringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationMembershipRepository membershipRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Organization organization;
    private User adminUser;
    private User regularUser;
    private String adminToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> organizationRepository.save(new Organization(UUID.randomUUID(), "Test HQ", "TEST-HQ")));

        adminUser = userRepository.findByEmail("admin.test@processhub.com")
                .orElseGet(() -> {
                    User u = new User(UUID.randomUUID(), "admin.test@processhub.com", passwordEncoder.encode("Password123!"), "Admin User", "111", Role.ROLE_ADMIN, "OPERATIONS");
                    userRepository.save(u);
                    membershipRepository.save(new OrganizationMembership(UUID.randomUUID(), organization, u, null, Role.ROLE_ADMIN));
                    return u;
                });

        regularUser = userRepository.findByEmail("reporter.test@processhub.com")
                .orElseGet(() -> {
                    User u = new User(UUID.randomUUID(), "reporter.test@processhub.com", passwordEncoder.encode("Password123!"), "Reporter User", "222", Role.ROLE_REPORTER, "FACILITIES");
                    userRepository.save(u);
                    membershipRepository.save(new OrganizationMembership(UUID.randomUUID(), organization, u, null, Role.ROLE_REPORTER));
                    return u;
                });

        adminToken = jwtTokenProvider.generateToken(adminUser);
        regularUserToken = jwtTokenProvider.generateToken(regularUser);
    }

    @Test
    @DisplayName("Admin should successfully fetch organization users monitoring list")
    void testGetOrganizationUsersAsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{orgId}/admin/users", organization.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].email", hasItem(adminUser.getEmail())));
    }

    @Test
    @DisplayName("Non-admin role should be denied access to organization users monitoring")
    void testGetOrganizationUsersAsNonAdminForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{orgId}/admin/users", organization.getId())
                        .header("Authorization", "Bearer " + regularUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should successfully fetch system activities timeline")
    void testGetOrganizationActivitiesAsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{orgId}/admin/activities", organization.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .param("limit", "25")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @DisplayName("Admin should successfully fetch comprehensive system-wide report")
    void testGetSystemReportAsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{orgId}/admin/system-report", organization.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIssues", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.statusDistribution", notNullValue()))
                .andExpect(jsonPath("$.priorityDistribution", notNullValue()))
                .andExpect(jsonPath("$.departmentDistribution", notNullValue()))
                .andExpect(jsonPath("$.departmentMetrics", notNullValue()));
    }
}
