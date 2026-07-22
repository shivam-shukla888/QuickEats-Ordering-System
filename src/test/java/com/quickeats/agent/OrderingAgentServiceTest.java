
package com.quickeats.agent;

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
class OrderingAgentServiceTest {

    @Mock
    private MenuTools menuTools;

    @Mock
    private OrderTools orderTools;

    @InjectMocks
    private OrderingAgentService orderingAgentService;

    @Test
    void processMessage_SearchMenu_InvokesMenuTool() {
        when(menuTools.searchMenu(anyString(), any(), any())).thenReturn("Found 2 spicy dishes.");

        OrderingAgentService.AgentResponse response = orderingAgentService.processMessage(1L, "show me spicy food under 200 rupees");

        assertNotNull(response);
        assertTrue(response.getToolsInvoked().contains("searchMenu"));
        assertTrue(response.getResponse().contains("spicy"));
    }

    @Test
    void processMessage_PlaceOrder_InvokesOrderTool() {
        when(orderTools.placeOrder(anyLong(), anyList())).thenReturn("Order #101 Placed Successfully!");

        OrderingAgentService.AgentResponse response = orderingAgentService.processMessage(1L, "order item 1");

        assertNotNull(response);
        assertTrue(response.getToolsInvoked().contains("placeOrder"));
        assertTrue(response.getResponse().contains("Order #101"));
    }

    @Test
    void processMessage_CheckStatus_InvokesStatusTool() {
        when(orderTools.checkOrderStatus(anyLong())).thenReturn("Order #101 Status: PREPARING");

        OrderingAgentService.AgentResponse response = orderingAgentService.processMessage(1L, "check status of order 101");

        assertNotNull(response);
        assertTrue(response.getToolsInvoked().contains("checkOrderStatus"));
        assertTrue(response.getResponse().contains("PREPARING"));
    }

    @Test
    void processMessage_CancelOrder_InvokesCancelTool() {
        when(orderTools.cancelOrder(anyLong())).thenReturn("Order #101 Cancelled Successfully");

        OrderingAgentService.AgentResponse response = orderingAgentService.processMessage(1L, "cancel order 101");

        assertNotNull(response);
        assertTrue(response.getToolsInvoked().contains("cancelOrder"));
        assertTrue(response.getResponse().contains("Cancelled"));
    }
}
