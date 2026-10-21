package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.Department;
import com.processhub.domain.entity.Organization;
import com.processhub.domain.entity.User;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.DepartmentRepository;
import com.processhub.domain.repository.OrganizationMembershipRepository;
import com.processhub.domain.repository.OrganizationRepository;
import com.processhub.domain.repository.UserRepository;
import com.processhub.dto.CreateMembershipRequest;
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
class MembershipManagementIntegrationTest {

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

    private Organization organization;
    private Department facilitiesDept;
    private Department plumbingDept;
    private User adminUser;
    private String adminToken;
    private User staffUser;
    private String staffToken;

    @BeforeEach
    void setUp() {
        membershipRepository.deleteAll();
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();

        organization = organizationRepository.save(new Organization(UUID.randomUUID(), "Campus Operations", "CAMPUS-1"));

        facilitiesDept = departmentRepository.save(
                new Department(UUID.randomUUID(), organization, "Facilities", "FACILITIES", "Structural and HVAC"));

        plumbingDept = departmentRepository.save(
                new Department(UUID.randomUUID(), organization, "Plumbing", "PLUMBING", "Water and pipes"));

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

        staffUser = new User(
                UUID.randomUUID(),
                "staff.member@processhub.com",
                passwordEncoder.encode("StaffPass123!"),
                "Staff Member",
                null,
                Role.ROLE_REPORTER,
                null
        );
        userRepository.save(staffUser);
        staffToken = jwtTokenProvider.generateToken(staffUser);
    }

    @Test
    @DisplayName("Admin assigns supervisor membership to a user in facilities department")
    void testAssignSupervisorMembership() throws Exception {
        CreateMembershipRequest request = new CreateMembershipRequest(
                staffUser.getId(),
                facilitiesDept.getId(),
                Role.ROLE_SUPERVISOR
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/memberships")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ROLE_SUPERVISOR"))
                .andExpect(jsonPath("$.userEmail").value("staff.member@processhub.com"))
                .andExpect(jsonPath("$.departmentName").value("Facilities"));
    }

    @Test
    @DisplayName("Admin assigns technician membership to a user in plumbing department")
    void testAssignTechnicianMembership() throws Exception {
        CreateMembershipRequest request = new CreateMembershipRequest(
                staffUser.getId(),
                plumbingDept.getId(),
                Role.ROLE_TECHNICIAN
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/memberships")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_TECHNICIAN"))
                .andExpect(jsonPath("$.departmentName").value("Plumbing"));
    }

    @Test
    @DisplayName("Non-admin user cannot assign memberships (403 Forbidden)")
    void testNonAdminCannotAssignMembership() throws Exception {
        CreateMembershipRequest request = new CreateMembershipRequest(
                staffUser.getId(),
                facilitiesDept.getId(),
                Role.ROLE_SUPERVISOR
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/memberships")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("User can retrieve their active memberships across organizations")
    void testGetUserMemberships() throws Exception {
        CreateMembershipRequest req = new CreateMembershipRequest(
                staffUser.getId(),
                facilitiesDept.getId(),
                Role.ROLE_SUPERVISOR
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/memberships")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users/me/memberships")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].organizationName").value("Campus Operations"))
                .andExpect(jsonPath("$[0].departmentName").value("Facilities"))
                .andExpect(jsonPath("$[0].role").value("ROLE_SUPERVISOR"));
    }
}
