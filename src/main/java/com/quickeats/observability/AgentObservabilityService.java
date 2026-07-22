package com.quickeats.observability;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgentObservabilityService {

    @Autowired
    private AgentCallLogRepository agentCallLogRepository;

    public Map<String, Object> getAggregatedMetrics() {
        List<AgentCallLog> allLogs = agentCallLogRepository.findAll();
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        List<AgentCallLog> logs24h = agentCallLogRepository.findByTimestampAfter(twentyFourHoursAgo);

        long totalCalls = allLogs.size();
        long successCount = allLogs.stream().filter(AgentCallLog::isSuccess).count();
        double successRate = totalCalls > 0 ? (double) successCount / totalCalls * 100.0 : 100.0;

        double avgLatencyMs = allLogs.stream()
                .mapToLong(AgentCallLog::getLatencyMs)
                .average()
                .orElse(0.0);

        int totalTokens = allLogs.stream()
                .mapToInt(l -> l.getTokenUsage() != null ? l.getTokenUsage() : 0)
                .sum();

        // Calls per agent type
        Map<String, Long> callsByAgentType = allLogs.stream()
                .collect(Collectors.groupingBy(AgentCallLog::getAgentType, Collectors.counting()));

        // Average latency per agent type
        Map<String, Double> avgLatencyByAgentType = allLogs.stream()
                .collect(Collectors.groupingBy(
                        AgentCallLog::getAgentType,
                        Collectors.averagingLong(AgentCallLog::getLatencyMs)
                ));

        // Most used tools
        Map<String, Long> toolCounts = allLogs.stream()
                .filter(l -> l.getToolsInvoked() != null)
                .collect(Collectors.groupingBy(AgentCallLog::getToolsInvoked, Collectors.counting()));

        Map<String, Object> response = new HashMap<>();
        response.put("totalCalls", totalCalls);
        response.put("callsLast24Hours", logs24h.size());
        response.put("overallSuccessRatePercent", Math.round(successRate * 100.0) / 100.0);
        response.put("averageLatencyMs", Math.round(avgLatencyMs * 100.0) / 100.0);
        response.put("totalEstimatedTokens", totalTokens);
        response.put("callsByAgentType", callsByAgentType);
        response.put("avgLatencyByAgentType", avgLatencyByAgentType);
        response.put("mostUsedTools", toolCounts);
        response.put("recentLogs", agentCallLogRepository.findTop50ByOrderByTimestampDesc());

        return response;
    }

    public List<AgentCallLog> getRecentLogs() {
        return agentCallLogRepository.findTop50ByOrderByTimestampDesc();
    }
}
