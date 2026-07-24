package com.quickeats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickeats.dto.SupportChatRequestDTO;
import com.quickeats.model.Order;
import com.quickeats.model.Restaurant;
import com.quickeats.model.User;
import com.quickeats.repository.OrderRepository;
import com.quickeats.repository.UserRepository;
import com.quickeats.service.GroqClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatSupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroqClientService groqClientService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrderRepository orderRepository;

    private User sampleUser;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Rahul Sharma", "rahul@example.com", "password", "CUSTOMER");
        sampleUser.setId(1L);

        Restaurant sampleRestaurant = new Restaurant("Punjab Dhaba", "Delhi", "North Indian");
        sampleRestaurant.setId(10L);

        sampleOrder = new Order(sampleUser, sampleRestaurant, 25.50, "Butter Chicken, Garlic Naan");
        sampleOrder.setId(100L);
        sampleOrder.setStatus("IN_TRANSIT");
        sampleOrder.setOrderTime(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(orderRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(sampleOrder)));
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(sampleOrder));
    }

    @Test
    void testSupportChat_Success() throws Exception {
        when(groqClientService.chatCompletion(anyString(), anyString()))
                .thenReturn("Your order #100 from Punjab Dhaba is currently in transit and arriving in ~15 minutes!");

        SupportChatRequestDTO request = new SupportChatRequestDTO(1L, "Where is my order?");

        mockMvc.perform(post("/api/chat/support")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", containsString("Punjab Dhaba")))
                .andExpect(jsonPath("$.response", containsString("in transit")));
    }

    @Test
    void testSupportChat_FallbackOnGroqError() throws Exception {
        when(groqClientService.chatCompletion(anyString(), anyString()))
                .thenThrow(new RuntimeException("Groq API Timeout"));

        SupportChatRequestDTO request = new SupportChatRequestDTO(1L, "Where is my order?");

        mockMvc.perform(post("/api/chat/support")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", containsString("IN_TRANSIT")))
                .andExpect(jsonPath("$.response", containsString("Punjab Dhaba")));
    }
}
