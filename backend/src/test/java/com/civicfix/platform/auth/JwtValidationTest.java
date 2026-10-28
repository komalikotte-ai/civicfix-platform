package com.civicfix.platform.auth;

import com.civicfix.platform.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtValidationTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "civicfixSuperSecretKeyForDevelopmentMustBe32CharactersOrLonger";
    private static final long EXPIRATION_MS = 60000; // 1 minute
    private static final long REFRESH_EXPIRATION_MS = 120000; // 2 minutes

    @BeforeEach
    void setUp() {
        this.jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION_MS, REFRESH_EXPIRATION_MS);
    }

    @Test
    @DisplayName("JWT Validation - Generates valid token with claims and validates successfully")
    void testTokenGenerationAndValidation() {
        Long userId = 101L;
        String username = "citizen_anil";
        Long orgId = 1L;
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_CITIZEN"),
                new SimpleGrantedAuthority("COMPLAINT_CREATE")
        );

        String token = jwtTokenProvider.generateToken(userId, username, authorities, orgId);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));

        Claims claims = jwtTokenProvider.getClaimsFromToken(token);
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals(userId.intValue(), claims.get("userId"));
        assertEquals(orgId.intValue(), claims.get("orgId"));

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertTrue(roles.contains("ROLE_CITIZEN"));

        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) claims.get("permissions");
        assertTrue(permissions.contains("COMPLAINT_CREATE"));
    }

    @Test
    @DisplayName("JWT Validation - Rejects tampered, malformed, or invalid tokens")
    void testTamperedAndMalformedTokenRejection() {
        assertFalse(jwtTokenProvider.validateToken("not.a.valid.jwt.token"));
        assertFalse(jwtTokenProvider.validateToken(""));
        assertFalse(jwtTokenProvider.validateToken(null));

        String validToken = jwtTokenProvider.generateToken(1L, "user1", List.of(), 1L);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "abcde";
        assertFalse(jwtTokenProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("JWT Validation - Rejects expired token")
    void testExpiredTokenValidation() {
        // Provider configured with past expiration (-10 seconds)
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -10000L, -5000L);
        String token = expiredProvider.generateToken(2L, "expired_user", List.of(), null);

        assertFalse(expiredProvider.validateToken(token));
    }
}
