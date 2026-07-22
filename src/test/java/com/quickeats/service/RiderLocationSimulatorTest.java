package com.quickeats.service;

import com.quickeats.model.Order;
import com.quickeats.model.Restaurant;
import com.quickeats.model.User;
import com.quickeats.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RiderLocationSimulatorTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private RiderLocationSimulatorService simulatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void simulateRiderMovement_UpdatesAndBroadcastsRiderLocation() {
        User user = new User("Rohan Sharma", "rohan@test.com", "password", "CUSTOMER");
        user.setId(5L);
        Restaurant restaurant = new Restaurant("Punjab Dhaba", "12 Lal Chowk", "North Indian");
        restaurant.setId(10L);

        Order order = new Order(user, restaurant, 19.99, "[]");
        order.setId(50L);
        order.setStatus("OUT_FOR_DELIVERY");

        when(orderRepository.findAll()).thenReturn(List.of(order));

        simulatorService.simulateRiderMovement();

        verify(orderService).broadcastStatusUpdate(any(Order.class), anyInt(), anyDouble(), anyDouble());
        assertTrue(simulatorService.getOrderProgressMap().containsKey(50L));
    }
}
