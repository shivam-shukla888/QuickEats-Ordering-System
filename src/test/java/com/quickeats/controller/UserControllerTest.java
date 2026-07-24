package com.quickeats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickeats.dto.AuthRequestDTO;
import com.quickeats.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.quickeats.repository.RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerUser_HappyPath_Returns201Created() throws Exception {
        Map<String, String> registerRequest = Map.of(
                "name", "Alice Walker",
                "email", "alice@example.com",
                "password", "securePass123",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.id").exists())
                .andExpect(jsonPath("$.user.email", is("alice@example.com")))
                .andExpect(jsonPath("$.user.name", is("Alice Walker")))
                .andExpect(jsonPath("$.user.role", is("CUSTOMER")))
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void registerUser_DuplicateEmail_Returns400BadRequest() throws Exception {
        Map<String, String> registerRequest1 = Map.of(
                "name", "Alice Walker",
                "email", "duplicate@example.com",
                "password", "securePass123",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest1)))
                .andExpect(status().isCreated());

        Map<String, String> registerRequest2 = Map.of(
                "name", "Alice Duplicate",
                "email", "duplicate@example.com",
                "password", "anotherPass123",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Email already exists")));
    }

    @Test
    void login_HappyPath_ReturnsJwtToken() throws Exception {
        Map<String, String> registerRequest = Map.of(
                "name", "Bob Smith",
                "email", "bob@example.com",
                "password", "mySecretPass",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        AuthRequestDTO loginRequest = new AuthRequestDTO("bob@example.com", "mySecretPass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.user.email", is("bob@example.com")));
    }
}
