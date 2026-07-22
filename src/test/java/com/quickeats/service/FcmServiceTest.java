package com.quickeats.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FcmServiceTest {

    @InjectMocks
    private FcmService fcmService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendPushNotification_WithNullOrEmptyToken_DoesNotThrowException() {
        assertDoesNotThrow(() -> fcmService.sendPushNotification(null, "Title", "Body"));
        assertDoesNotThrow(() -> fcmService.sendPushNotification("", "Title", "Body"));
        assertDoesNotThrow(() -> fcmService.sendPushNotification("   ", "Title", "Body"));
    }

    @Test
    void sendPushNotification_UninitializedFirebase_HandledGracefully() {
        assertDoesNotThrow(() -> fcmService.sendPushNotification("mock_device_token_123", "Title", "Body"));
    }
}
