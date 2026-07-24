package com.quickeats.service;

import com.quickeats.dto.CreateOrderDTO;
import com.quickeats.dto.OrderItemDTO;
import com.quickeats.dto.OrderResponseDTO;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.Menu;
import com.quickeats.model.Order;
import com.quickeats.model.Restaurant;
import com.quickeats.model.User;
import com.quickeats.repository.MenuRepository;
import com.quickeats.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private UserService userService;

    @Mock
    private RestaurantService restaurantService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OrderService orderService;

    private User sampleUser;
    private Restaurant sampleRestaurant;
    private Menu sampleMenu;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Jane Smith", "jane@example.com", "pass123", "CUSTOMER");
        sampleUser.setId(10L);

        sampleRestaurant = new Restaurant("Burger King", "456 Oak Ave", "American");
        sampleRestaurant.setId(20L);

        sampleMenu = new Menu(sampleRestaurant, "Cheeseburger", 12.50, "Delicious burger");
        sampleMenu.setId(100L);
    }

    @Test
    void placeOrder_Success() {
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setUserId(10L);
        createOrderDTO.setRestaurantId(20L);
        OrderItemDTO item = new OrderItemDTO(100L, 2, 12.50, "Cheeseburger");
        createOrderDTO.setItems(List.of(item));

        when(userService.getUserById(10L)).thenReturn(Optional.of(sampleUser));
        when(restaurantService.getRestaurantEntityById(20L)).thenReturn(Optional.of(sampleRestaurant));
        when(menuRepository.findById(100L)).thenReturn(Optional.of(sampleMenu));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1001L);
            return o;
        });

        OrderResponseDTO responseDTO = orderService.placeOrder(createOrderDTO);

        assertNotNull(responseDTO);
        assertEquals(1001L, responseDTO.getId());
        assertEquals(25.0, responseDTO.getTotalAmount());
        assertEquals("PENDING", responseDTO.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void placeOrder_PriceTamperingIgnored_UsesMenuPrice() {
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setUserId(10L);
        createOrderDTO.setRestaurantId(20L);
        // Client attempts to send fake price 0.01 instead of DB price 12.50
        OrderItemDTO item = new OrderItemDTO(100L, 2, 0.01, "Cheeseburger");
        createOrderDTO.setItems(List.of(item));

        when(userService.getUserById(10L)).thenReturn(Optional.of(sampleUser));
        when(restaurantService.getRestaurantEntityById(20L)).thenReturn(Optional.of(sampleRestaurant));
        when(menuRepository.findById(100L)).thenReturn(Optional.of(sampleMenu));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1001L);
            return o;
        });

        OrderResponseDTO responseDTO = orderService.placeOrder(createOrderDTO);

        assertNotNull(responseDTO);
        assertEquals(25.0, responseDTO.getTotalAmount(), "Total amount should be recomputed using DB price (12.50 * 2)");
    }

    @Test
    void placeOrder_MenuNotFound_ThrowsException() {
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setUserId(10L);
        createOrderDTO.setRestaurantId(20L);
        OrderItemDTO item = new OrderItemDTO(999L, 1, 10.0, "Unknown Item");
        createOrderDTO.setItems(List.of(item));

        when(userService.getUserById(10L)).thenReturn(Optional.of(sampleUser));
        when(restaurantService.getRestaurantEntityById(20L)).thenReturn(Optional.of(sampleRestaurant));
        when(menuRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.placeOrder(createOrderDTO));
    }

    @Test
    void placeOrder_MenuBelongsToOtherRestaurant_ThrowsException() {
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setUserId(10L);
        createOrderDTO.setRestaurantId(20L);

        Restaurant foreignRestaurant = new Restaurant("Pizza Hut", "123 Main St", "Italian");
        foreignRestaurant.setId(99L);
        Menu foreignMenu = new Menu(foreignRestaurant, "Pepperoni Pizza", 15.0, "Pizza");
        foreignMenu.setId(200L);

        OrderItemDTO item = new OrderItemDTO(200L, 1, 15.0, "Pepperoni Pizza");
        createOrderDTO.setItems(List.of(item));

        when(userService.getUserById(10L)).thenReturn(Optional.of(sampleUser));
        when(restaurantService.getRestaurantEntityById(20L)).thenReturn(Optional.of(sampleRestaurant));
        when(menuRepository.findById(200L)).thenReturn(Optional.of(foreignMenu));

        assertThrows(ResourceNotFoundException.class, () -> orderService.placeOrder(createOrderDTO));
    }

    @Test
    void placeOrder_UserNotFound_ThrowsException() {
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setUserId(99L);
        createOrderDTO.setRestaurantId(20L);

        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.placeOrder(createOrderDTO));
    }

    @Test
    void updateOrderStatus_InvalidStatus_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> orderService.updateOrderStatus(1001L, "INVALID_STATUS"));
    }

    @Test
    void updateOrderStatus_ValidStatus_Success() {
        Order order = new Order(sampleUser, sampleRestaurant, 25.0, "[]");
        order.setId(1001L);
        order.setStatus("PENDING");

        when(orderRepository.findById(1001L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDTO dto = orderService.updateOrderStatus(1001L, "preparing");
        assertNotNull(dto);
        assertEquals("PREPARING", dto.getStatus());
    }
}

