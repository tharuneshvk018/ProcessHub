package com.processhub.security;

import com.processhub.domain.entity.User;
import com.processhub.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate and validate JWT token successfully")
    void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "engineer@processhub.com", "hash", "Operational Engineer", "1234567890", Role.ROLE_REPORTER, "FACILITIES");

        String token = tokenProvider.generateToken(user);
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));

        assertEquals(userId, tokenProvider.getUserIdFromToken(token));
        assertEquals("engineer@processhub.com", tokenProvider.getEmailFromToken(token));
        assertEquals(Role.ROLE_REPORTER, tokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Should reject invalid or malformed JWT token")
    void testRejectInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid.token.structure"));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken(null));
    }

    @Test
    @DisplayName("Should reject expired JWT token")
    void testRejectExpiredToken() {
        // Create token provider with 1 millisecond expiration
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secret, 1L);
        User user = new User(UUID.randomUUID(), "expired@processhub.com", "hash", "Expired User", null, Role.ROLE_REPORTER, null);

        String token = shortLivedProvider.generateToken(user);

        // Sleep briefly to ensure expiry
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}

        assertFalse(shortLivedProvider.validateToken(token));
    }
}
