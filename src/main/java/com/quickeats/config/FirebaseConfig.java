package com.quickeats.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-path:classpath:firebase-service-account.json}")
    private String serviceAccountPath;

    @Value("${firebase.project-id:quickeats-2e295}")
    private String projectId;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                Resource resource = resourceLoader.getResource(serviceAccountPath);
                if (resource.exists()) {
                    try (InputStream serviceAccount = resource.getInputStream()) {
                        FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .setProjectId(projectId)
                                .build();

                        FirebaseApp.initializeApp(options);
                        logger.info("Firebase Admin SDK initialized successfully for project '{}'.", projectId);
                    }
                } else {
                    logger.warn("Firebase service account file at '{}' not found. Skipping FCM init.", serviceAccountPath);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
        }
    }
}
