package com.quickeats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickeats.model.Order;
import com.quickeats.model.Restaurant;
import com.quickeats.model.User;
import com.quickeats.repository.OrderRepository;
import com.quickeats.repository.RefreshTokenRepository;
import com.quickeats.repository.RestaurantRepository;
import com.quickeats.repository.UserRepository;
import com.quickeats.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private User customerUser;
    private User otherCustomerUser;
    private User adminUser;
    private Restaurant sampleRestaurant;
    private Order sampleOrder;

    private String customerToken;
    private String otherCustomerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        orderRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();

        customerUser = userRepository.save(new User("Customer User", "customer@example.com", passwordEncoder.encode("pass123"), "CUSTOMER"));
        otherCustomerUser = userRepository.save(new User("Other User", "other@example.com", passwordEncoder.encode("pass123"), "CUSTOMER"));
        adminUser = userRepository.save(new User("Admin User", "admin@example.com", passwordEncoder.encode("pass123"), "ADMIN"));

        customerToken = jwtUtil.generateToken(customerUser.getEmail(), customerUser.getRole());
        otherCustomerToken = jwtUtil.generateToken(otherCustomerUser.getEmail(), otherCustomerUser.getRole());
        adminToken = jwtUtil.generateToken(adminUser.getEmail(), adminUser.getRole());

        sampleRestaurant = restaurantRepository.save(new Restaurant("Tandoor Hut", "123 Street", "Indian"));
        sampleOrder = orderRepository.save(new Order(customerUser, sampleRestaurant, 50.0, "[]"));
    }

    @Test
    void getOrdersByUser_OtherUser_Returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/orders/user/" + customerUser.getId())
                .header("Authorization", "Bearer " + otherCustomerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrderStatus_PlainCustomer_Returns403Forbidden() throws Exception {
        Map<String, String> statusReq = Map.of("status", "PREPARING");

        mockMvc.perform(put("/api/orders/" + sampleOrder.getId() + "/status")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelOrder_CustomerOwnPendingOrder_Returns200OK() throws Exception {
        mockMvc.perform(put("/api/orders/" + sampleOrder.getId() + "/cancel")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrder_CustomerOtherUserOrder_Returns403Forbidden() throws Exception {
        mockMvc.perform(put("/api/orders/" + sampleOrder.getId() + "/cancel")
                .header("Authorization", "Bearer " + otherCustomerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrderStatus_AdminUser_Success() throws Exception {
        Map<String, String> statusReq = Map.of("status", "PREPARING");

        mockMvc.perform(put("/api/orders/" + sampleOrder.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk());
    }
}
