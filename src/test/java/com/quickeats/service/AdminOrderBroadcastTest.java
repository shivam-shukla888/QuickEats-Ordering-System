package com.quickeats.service;

import com.quickeats.model.Order;
import com.quickeats.model.Restaurant;
import com.quickeats.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class AdminOrderBroadcastTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void broadcastAdminOrderEvent_SendsStompMessageToAdminTopic() {
        User user = new User("Priya Patel", "priya@test.com", "password", "CUSTOMER");
        user.setId(12L);
        Restaurant restaurant = new Restaurant("Pind Balluchi", "45 Mall Road", "North Indian");
        restaurant.setId(22L);

        Order order = new Order(user, restaurant, 42.00, "[]");
        order.setId(200L);
        order.setStatus("PENDING");

        orderService.broadcastAdminOrderEvent(order, 3);

        verify(messagingTemplate).convertAndSend(eq("/topic/admin/orders"), any(Object.class));
    }
}
