package com.quickeats.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @GetMapping
    public ResponseEntity<Map<String, String>> getHealth() {
        String dbStatus = (datasourceUrl != null && datasourceUrl.contains("h2:mem")) ? "fallback-h2" : "connected";
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "db", dbStatus
        ));
    }
}
