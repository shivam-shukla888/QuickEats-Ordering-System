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

class OrderWebSocketTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void broadcastStatusUpdate_SendsStompMessageToTopic() {
        User user = new User("Test Customer", "customer@test.com", "password", "CUSTOMER");
        user.setId(10L);
        Restaurant restaurant = new Restaurant("Punjab Dhaba", "12 Lal Chowk, Delhi", "North Indian");
        restaurant.setId(20L);

        Order order = new Order(user, restaurant, 25.50, "[]");
        order.setId(100L);
        order.setStatus("OUT_FOR_DELIVERY");

        orderService.broadcastStatusUpdate(order, 15, 28.6289, 77.2185);

        verify(messagingTemplate).convertAndSend(eq("/topic/orders/100"), any(Object.class));
    }
}
