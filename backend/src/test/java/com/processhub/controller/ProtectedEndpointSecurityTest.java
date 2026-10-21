package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.entity.User;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.UserRepository;
import com.processhub.dto.RegisterRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProtectedEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint should return 401 Unauthorized")
    void testUnauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/test/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Publicly registered user has ROLE_REPORTER and can access general user endpoint")
    void testReporterAccessToUserEndpoint() throws Exception {
        RegisterRequest request = new RegisterRequest("reporter@processhub.com", "Password123!", "Regular Reporter", null);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/v1/test/user")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    @DisplayName("SAFEGUARD 2: Public registration must never grant admin privileges; reporter denied admin endpoint (403 Forbidden)")
    void testReporterDeniedAdminEndpoint() throws Exception {
        RegisterRequest request = new RegisterRequest("attacker@processhub.com", "Password123!", "Malicious Actor", null);

        // Attempting to send JSON with unexpected role field: 'role': 'ROLE_ADMIN'
        String maliciousJson = "{\"email\":\"attacker@processhub.com\",\"password\":\"Password123!\",\"fullName\":\"Malicious Actor\",\"role\":\"ROLE_ADMIN\"}";

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maliciousJson))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();

        // Verify in DB that assigned role is strictly ROLE_REPORTER
        User savedUser = userRepository.findByEmail("attacker@processhub.com").orElseThrow();
        assertEquals(Role.ROLE_REPORTER, savedUser.getRole());

        // Access to admin endpoint MUST be 403 Forbidden
        mockMvc.perform(get("/api/v1/test/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("permission")));
    }

    @Test
    @DisplayName("Privileged Administrator account can access admin endpoint (200 OK)")
    void testAdminAccessAllowed() throws Exception {
        // Provision verified admin user into repository
        User admin = new User(
                UUID.randomUUID(),
                "admin@processhub.com",
                passwordEncoder.encode("AdminSecret123!"),
                "System Administrator",
                null,
                Role.ROLE_ADMIN,
                "ADMINISTRATION"
        );
        userRepository.save(admin);

        String adminToken = jwtTokenProvider.generateToken(admin);

        mockMvc.perform(get("/api/v1/test/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("Administrator")));
    }

    @Test
    @DisplayName("Supervisor account can access supervisor endpoint and is denied admin endpoint")
    void testSupervisorRoleBoundaries() throws Exception {
        User supervisor = new User(
                UUID.randomUUID(),
                "supervisor@processhub.com",
                passwordEncoder.encode("SupervisorPass1!"),
                "Facilities Supervisor",
                null,
                Role.ROLE_SUPERVISOR,
                "FACILITIES"
        );
        userRepository.save(supervisor);

        String supervisorToken = jwtTokenProvider.generateToken(supervisor);

        // Access supervisor endpoint -> 200 OK
        mockMvc.perform(get("/api/v1/test/supervisor")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        // Attempt admin endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/v1/test/admin")
                        .header("Authorization", "Bearer " + supervisorToken))
                .andExpect(status().isForbidden());
    }
}
