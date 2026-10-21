package com.processhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.processhub.domain.enums.Role;
import com.processhub.domain.repository.UserRepository;
import com.processhub.dto.LoginRequest;
import com.processhub.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully register a new reporter user")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "sarah.connor@processhub.com",
                "SecurePassword123!",
                "Sarah Connor",
                "+1-555-0199"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("sarah.connor@processhub.com"))
                .andExpect(jsonPath("$.fullName").value("Sarah Connor"))
                .andExpect(jsonPath("$.role").value("ROLE_REPORTER"));

        // Verify in repository that role is strictly ROLE_REPORTER
        var user = userRepository.findByEmail("sarah.connor@processhub.com").orElseThrow();
        assertEquals(Role.ROLE_REPORTER, user.getRole());
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "alex.murphy@processhub.com",
                "SecurePassword123!",
                "Alex Murphy",
                null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Attempt second registration with same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Should reject registration with invalid fields")
    void testRegisterValidationFailure() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "not-an-email",
                "short", // Less than 8 characters
                "",      // Blank name
                null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("Should successfully authenticate with valid credentials")
    void testLoginSuccess() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "elena.rostova@processhub.com",
                "FacilityPass2026!",
                "Elena Rostova",
                "+1-555-4321"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("elena.rostova@processhub.com", "FacilityPass2026!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.email").value("elena.rostova@processhub.com"))
                .andExpect(jsonPath("$.role").value("ROLE_REPORTER"));
    }

    @Test
    @DisplayName("Should reject authentication with incorrect password")
    void testLoginWrongPassword() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "john.doe@processhub.com",
                "CorrectPassword123!",
                "John Doe",
                null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("john.doe@processhub.com", "WrongPassword999!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }

    @Test
    @DisplayName("Should access /me with valid Bearer token and return user profile")
    void testGetCurrentUserWithToken() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "marcus.wright@processhub.com",
                "StrongSecretPass1!",
                "Marcus Wright",
                "+1-555-8822"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(json).get("token").asText();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("marcus.wright@processhub.com"))
                .andExpect(jsonPath("$.fullName").value("Marcus Wright"))
                .andExpect(jsonPath("$.role").value("ROLE_REPORTER"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should reject /me without Bearer token")
    void testGetCurrentUserUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
