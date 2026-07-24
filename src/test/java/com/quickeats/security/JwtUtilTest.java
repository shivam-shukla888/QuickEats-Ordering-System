package com.quickeats.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void validateSecretKey_WhenSecretIsMissingOrShort_SetsFallbackSecret() {
        JwtUtil jwtUtil = new JwtUtil();

        // 1. Null or empty secret
        ReflectionTestUtils.setField(jwtUtil, "secret", "");
        jwtUtil.validateSecretKey();
        String activeSecret1 = (String) ReflectionTestUtils.getField(jwtUtil, "secret");
        assertNotNull(activeSecret1);
        assertTrue(activeSecret1.length() >= 32);

        // 2. Secret shorter than 256 bits (less than 32 bytes)
        ReflectionTestUtils.setField(jwtUtil, "secret", "short_secret_under_32_bytes");
        jwtUtil.validateSecretKey();
        String activeSecret2 = (String) ReflectionTestUtils.getField(jwtUtil, "secret");
        assertNotNull(activeSecret2);
        assertTrue(activeSecret2.length() >= 32);
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
