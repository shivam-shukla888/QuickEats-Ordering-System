package com.quickeats.service;

import com.quickeats.model.RefreshToken;
import com.quickeats.model.User;
import com.quickeats.repository.RefreshTokenRepository;
import com.quickeats.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 2592000000L);
        sampleUser = new User("Shivam Shukla", "shivam@example.com", "pass123", "CUSTOMER");
        sampleUser.setId(10L);
    }

    @Test
    void testCreateRefreshToken_IssuesValidToken() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken(10L);

        assertNotNull(token);
        assertNotNull(token.getToken());
        assertEquals(sampleUser, token.getUser());
        assertTrue(token.getExpiryDate().isAfter(Instant.now()));
        verify(refreshTokenRepository, times(1)).deleteByUser(sampleUser);
    }

    @Test
    void testVerifyExpiration_ValidToken_ReturnsToken() {
        RefreshToken token = new RefreshToken("uuid-1234", sampleUser, Instant.now().plusSeconds(3600));

        RefreshToken verified = refreshTokenService.verifyExpiration(token);

        assertNotNull(verified);
        assertEquals("uuid-1234", verified.getToken());
    }

    @Test
    void testVerifyExpiration_ExpiredToken_ThrowsExceptionAndDeletesToken() {
        RefreshToken expiredToken = new RefreshToken("expired-uuid", sampleUser, Instant.now().minusSeconds(3600));

        assertThrows(RuntimeException.class, () -> refreshTokenService.verifyExpiration(expiredToken));
        verify(refreshTokenRepository, times(1)).delete(expiredToken);
    }

    @Test
    void testLogout_InvalidatesRefreshToken() {
        when(refreshTokenRepository.deleteByToken("test-token")).thenReturn(1);

        int deletedCount = refreshTokenService.deleteByToken("test-token");

        assertEquals(1, deletedCount);
        verify(refreshTokenRepository, times(1)).deleteByToken("test-token");
    }
}
