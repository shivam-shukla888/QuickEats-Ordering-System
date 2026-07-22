package com.quickeats.controller;

import com.quickeats.dto.DeviceTokenRequestDTO;
import com.quickeats.model.User;
import com.quickeats.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register-token")
    public ResponseEntity<?> registerDeviceToken(@Valid @RequestBody DeviceTokenRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "User must be authenticated to register FCM token."));
        }

        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found."));
        }

        user.setFcmDeviceToken(request.getDeviceToken());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "FCM device token registered successfully.",
                "userEmail", userEmail
        ));
    }
}
