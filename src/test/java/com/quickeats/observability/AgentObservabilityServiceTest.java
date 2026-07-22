package com.quickeats.observability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentObservabilityServiceTest {

    @Mock
    private AgentCallLogRepository agentCallLogRepository;

    @InjectMocks
    private AgentObservabilityService agentObservabilityService;

    private AgentCallLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = new AgentCallLog("ORDERING", 1L, "Order biryani", "processUserMessage", 450L, 80, true, null);
    }

    @Test
    void testGetAggregatedMetrics_ReturnsValidStatistics() {
        when(agentCallLogRepository.findAll()).thenReturn(List.of(sampleLog));
        when(agentCallLogRepository.findByTimestampAfter(any(LocalDateTime.class))).thenReturn(List.of(sampleLog));
        when(agentCallLogRepository.findTop50ByOrderByTimestampDesc()).thenReturn(List.of(sampleLog));

        Map<String, Object> metrics = agentObservabilityService.getAggregatedMetrics();

        assertNotNull(metrics);
        assertEquals(1L, metrics.get("totalCalls"));
        assertEquals(100.0, metrics.get("overallSuccessRatePercent"));
        assertEquals(450.0, metrics.get("averageLatencyMs"));
    }

    @Test
    void testGetRecentLogs_ReturnsLogList() {
        when(agentCallLogRepository.findTop50ByOrderByTimestampDesc()).thenReturn(List.of(sampleLog));

        List<AgentCallLog> logs = agentObservabilityService.getRecentLogs();

        assertNotNull(logs);
        assertEquals(1, logs.size());
        assertEquals("ORDERING", logs.get(0).getAgentType());
    }
}
