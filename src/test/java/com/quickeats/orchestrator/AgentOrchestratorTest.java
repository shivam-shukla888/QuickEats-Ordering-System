package com.quickeats.orchestrator;

import com.quickeats.agent.OrderingAgentService;
import com.quickeats.rag.RecommendationService;
import com.quickeats.support.SupportAgentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentOrchestratorTest {

    @Mock
    private OrderingAgentService orderingAgentService;

    @Mock
    private SupportAgentService supportAgentService;

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private AgentOrchestrator agentOrchestrator;

    @Test
    void testOrchestrator_RoutesOrderingQueryToOrderingAgent() {
        OrderingAgentService.AgentResponse dummyResponse = new OrderingAgentService.AgentResponse("Found menu items", List.of("searchMenu"));
        when(orderingAgentService.processMessage(anyLong(), anyString())).thenReturn(dummyResponse);

        AgentOrchestrator.OrchestrationResult result = agentOrchestrator.routeAndExecute(1L, "search spicy food");

        assertNotNull(result);
        assertEquals("OrderingAgentService", result.getAgentHandled());
        assertEquals("ORDERING_SEARCH", result.getIntent());
    }

    @Test
    void testOrchestrator_RoutesComplaintToSupportAgent() {
        SupportAgentService.ComplaintResult complaintResult = new SupportAgentService.ComplaintResult("Investigation", "WRONG_ITEM_DELIVERED", "AUTO_REFUND_APPROVED", "Issued Refund", "Reasoning", List.of("issueRefund"));
        when(supportAgentService.handleComplaint(anyLong(), anyLong(), anyString())).thenReturn(complaintResult);

        AgentOrchestrator.OrchestrationResult result = agentOrchestrator.routeAndExecute(1L, "I want a refund for order 101 wrong item");

        assertNotNull(result);
        assertEquals("SupportAgentService", result.getAgentHandled());
        assertEquals("COMPLAINT_SUPPORT", result.getIntent());
    }
}
