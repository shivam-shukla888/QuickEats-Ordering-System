package com.quickeats.support;

import com.quickeats.agent.ToolInvocationTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportAgentServiceTest {

    @Mock
    private SupportTools supportTools;

    @InjectMocks
    private SupportAgentService supportAgentService;

    @BeforeEach
    void setUp() {
        ToolInvocationTracker.clear();
    }

    @Test
    void testSupportAgent_WrongItem_AutoApprovesRefundUnderThreshold() {
        when(supportTools.getOrderDetails(anyLong())).thenReturn("Order #1 details: Total ₹350.0");
        when(supportTools.checkDeliveryTimeline(anyLong())).thenReturn("Timeline OK");
        when(supportTools.issueRefund(anyLong(), anyDouble(), anyString())).thenAnswer(inv -> {
            ToolInvocationTracker.logToolCall("issueRefund");
            return "REFUND APPROVED: ₹350.0";
        });

        SupportAgentService.ComplaintResult result = supportAgentService.handleComplaint(1L, 1L, "wrong item delivered in order 1");

        assertNotNull(result);
        assertEquals("WRONG_ITEM_DELIVERED", result.getClassification());
        assertEquals("AUTO_REFUND_APPROVED", result.getDecision());
        assertTrue(result.getToolsInvoked().contains("issueRefund"));
    }

    @Test
    void testSupportAgent_HighValueComplaint_EscalatesExceedingThreshold() {
        when(supportTools.getOrderDetails(anyLong())).thenReturn("Order #2 details: Total ₹750.0");
        when(supportTools.escalateToHuman(anyLong(), anyString())).thenAnswer(inv -> {
            ToolInvocationTracker.logToolCall("escalateToHuman");
            return "ESCALATED TO SUPERVISOR";
        });

        SupportAgentService.ComplaintResult result = supportAgentService.handleComplaint(1L, 2L, "wrong expensive high value order 2 costing 750");

        assertNotNull(result);
        assertEquals("ESCALATE_TO_HUMAN", result.getDecision());
        assertTrue(result.getToolsInvoked().contains("escalateToHuman"));
    }

    @Test
    void testSupportAgent_AmbiguousComplaint_EscalatesToHuman() {
        when(supportTools.getOrderDetails(anyLong())).thenReturn("Order #3 details");
        when(supportTools.escalateToHuman(anyLong(), anyString())).thenAnswer(inv -> {
            ToolInvocationTracker.logToolCall("escalateToHuman");
            return "ESCALATED TO SUPERVISOR";
        });

        SupportAgentService.ComplaintResult result = supportAgentService.handleComplaint(1L, 3L, "hello maybe something was odd");

        assertNotNull(result);
        assertEquals("AMBIGUOUS_OR_UNVERIFIED", result.getClassification());
        assertEquals("ESCALATE_TO_HUMAN", result.getDecision());
        assertTrue(result.getToolsInvoked().contains("escalateToHuman"));
    }
}
