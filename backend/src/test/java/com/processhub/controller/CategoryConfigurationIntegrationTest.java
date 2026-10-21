package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.Department;
import com.processhub.domain.entity.Organization;
import com.processhub.domain.entity.User;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.DepartmentRepository;
import com.processhub.domain.repository.IssueCategoryRepository;
import com.processhub.domain.repository.OrganizationMembershipRepository;
import com.processhub.domain.repository.OrganizationRepository;
import com.processhub.domain.repository.UserRepository;
import com.processhub.dto.CreateCategoryRequest;
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
class CategoryConfigurationIntegrationTest {

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

    private Organization organization;
    private Department facilitiesDept;
    private Department itDept;
    private User adminUser;
    private String adminToken;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        departmentRepository.deleteAll();
        membershipRepository.deleteAll();
        organizationRepository.deleteAll();
        userRepository.deleteAll();

        organization = organizationRepository.save(new Organization(UUID.randomUUID(), "Science Campus", "SCI-CAMPUS"));

        facilitiesDept = departmentRepository.save(
                new Department(UUID.randomUUID(), organization, "Facilities", "FACILITIES", "Structural and HVAC"));

        itDept = departmentRepository.save(
                new Department(UUID.randomUUID(), organization, "IT Support", "IT", "Network and computing"));

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
    }

    @Test
    @DisplayName("Admin can configure category with subtypes and requirements")
    void testCreateCategoryWithSubtypes() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(
                facilitiesDept.getId(),
                "WATER_LEAKAGE",
                "Water Leakage & Flooding",
                "Plumbing failures, active leaks, or flood hazards",
                "Pipe Burst,Radiator Leak,Valve Drip,Ceiling Infiltration",
                "HIGH",
                false
        );

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("WATER_LEAKAGE"))
                .andExpect(jsonPath("$.name").value("Water Leakage & Flooding"))
                .andExpect(jsonPath("$.departmentName").value("Facilities"))
                .andExpect(jsonPath("$.subtypes", containsString("Pipe Burst")))
                .andExpect(jsonPath("$.defaultPriority").value("HIGH"));
    }

    @Test
    @DisplayName("Admin can filter categories by department")
    void testFilterCategoriesByDepartment() throws Exception {
        CreateCategoryRequest cat1 = new CreateCategoryRequest(
                facilitiesDept.getId(), "WATER_LEAK", "Water Leak", "Desc", "Drip,Burst", "HIGH", false);
        CreateCategoryRequest cat2 = new CreateCategoryRequest(
                itDept.getId(), "NETWORK_OUT", "Network Outage", "Desc", "Wi-Fi,Switch", "HIGH", true);

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat2)))
                .andExpect(status().isCreated());

        // Query only IT categories
        mockMvc.perform(get("/api/v1/organizations/" + organization.getId() + "/categories")
                        .param("departmentId", itDept.getId().toString())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value("NETWORK_OUT"))
                .andExpect(jsonPath("$[0].requiresAsset").value(true));
    }

    @Test
    @DisplayName("Duplicate category code in same organization fails (409 Conflict)")
    void testDuplicateCategoryCodeFails() throws Exception {
        CreateCategoryRequest cat = new CreateCategoryRequest(
                facilitiesDept.getId(), "WATER_LEAK", "Water Leak", "Desc", "Drip", "HIGH", false);

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/organizations/" + organization.getId() + "/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
