package com.quickeats.observability;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AgentObservabilityController {

    @Autowired
    private AgentObservabilityService agentObservabilityService;

    @GetMapping("/agent-metrics")
    public ResponseEntity<Map<String, Object>> getAgentMetrics() {
        return ResponseEntity.ok(agentObservabilityService.getAggregatedMetrics());
    }

    @GetMapping("/agent-logs")
    public ResponseEntity<List<AgentCallLog>> getAgentLogs() {
        return ResponseEntity.ok(agentObservabilityService.getRecentLogs());
    }
}
