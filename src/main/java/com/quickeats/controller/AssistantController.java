package com.quickeats.controller;

import com.quickeats.orchestrator.AgentOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
@CrossOrigin(origins = "*")
public class AssistantController {

    private static final Logger logger = LoggerFactory.getLogger(AssistantController.class);

    @Autowired
    private AgentOrchestrator agentOrchestrator;

    @PostMapping("/message")
    public ResponseEntity<AgentOrchestrator.OrchestrationResult> handleMessage(@RequestBody Map<String, Object> request) {
        Long userId = request.containsKey("userId") && request.get("userId") != null ?
                Long.parseLong(request.get("userId").toString()) : 1L;

        String message = request.containsKey("message") ? request.get("message").toString() : "";

        logger.info("Multi-Agent Assistant received message from User #{}: '{}'", userId, message);

        AgentOrchestrator.OrchestrationResult result = agentOrchestrator.routeAndExecute(userId, message);

        logger.info("Multi-Agent Orchestrator routed message to [{}] with intent '{}'", result.getAgentHandled(), result.getIntent());

        return ResponseEntity.ok(result);
    }
}
