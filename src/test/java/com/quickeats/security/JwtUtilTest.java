package com.quickeats.security;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilTest {

    @Test
    void validateSecretKey_WhenSecretIsMissingInNonDev_ThrowsIllegalStateException() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "");

        assertThrows(IllegalStateException.class, jwtUtil::validateSecretKey);
    }

    @Test
    void validateSecretKey_WhenSecretIsShortInNonDev_ThrowsIllegalStateException() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "short_secret_under_32_bytes");

        assertThrows(IllegalStateException.class, jwtUtil::validateSecretKey);
    }

    @Test
    void validateSecretKey_WhenSecretIsMissingInDev_SetsDevFallbackSecret() {
        JwtUtil jwtUtil = new JwtUtil();
        Environment env = mock(Environment.class);
        when(env.acceptsProfiles(Profiles.of("dev"))).thenReturn(true);
        ReflectionTestUtils.setField(jwtUtil, "environment", env);
        ReflectionTestUtils.setField(jwtUtil, "secret", "");

        assertDoesNotThrow(jwtUtil::validateSecretKey);
        String activeSecret = (String) ReflectionTestUtils.getField(jwtUtil, "secret");
        assertNotNull(activeSecret);
        assertTrue(activeSecret.getBytes().length >= 32);
    }

    @Test
    void validateSecretKey_WhenSecretIsValid_PassesValidation() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationInMs", 86400000L);

        assertDoesNotThrow(jwtUtil::validateSecretKey);

        String token = jwtUtil.generateToken("john.doe@example.com", "CUSTOMER");
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("john.doe@example.com", jwtUtil.getUsernameFromToken(token));
    }
}
