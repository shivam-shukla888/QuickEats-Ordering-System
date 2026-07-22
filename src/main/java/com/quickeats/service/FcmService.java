package com.quickeats.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    private static final Logger logger = LoggerFactory.getLogger(FcmService.class);

    public void sendPushNotification(String deviceToken, String title, String body) {
        if (deviceToken == null || deviceToken.isBlank()) {
            logger.debug("No FCM device token provided, skipping push notification.");
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            logger.debug("FirebaseApp is not initialized, skipping push notification.");
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notification)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("FCM push notification sent successfully to token [{}]: {}", deviceToken, response);
        } catch (Exception e) {
            logger.warn("FCM push notification failed for token [{}]: {}", deviceToken, e.getMessage());
        }
    }
}
