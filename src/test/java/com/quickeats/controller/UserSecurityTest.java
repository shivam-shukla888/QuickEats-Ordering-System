package com.quickeats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickeats.model.User;
import com.quickeats.repository.RefreshTokenRepository;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

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

    private String customerToken;
    private String otherCustomerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        customerUser = userRepository.save(new User("Customer User", "customer@example.com", passwordEncoder.encode("pass123"), "CUSTOMER"));
        otherCustomerUser = userRepository.save(new User("Other User", "other@example.com", passwordEncoder.encode("pass123"), "CUSTOMER"));
        adminUser = userRepository.save(new User("Admin User", "admin@example.com", passwordEncoder.encode("pass123"), "ADMIN"));

        customerToken = jwtUtil.generateToken(customerUser.getEmail(), customerUser.getRole());
        otherCustomerToken = jwtUtil.generateToken(otherCustomerUser.getEmail(), otherCustomerUser.getRole());
        adminToken = jwtUtil.generateToken(adminUser.getEmail(), adminUser.getRole());
    }

    @Test
    void register_AttemptRoleEscalation_ForcesCustomerRole() throws Exception {
        Map<String, String> regReq = Map.of(
                "name", "Attacker",
                "email", "attacker@example.com",
                "password", "password123",
                "role", "ADMIN"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role", is("CUSTOMER")));
    }

    @Test
    void updateUser_OtherUser_Returns403Forbidden() throws Exception {
        Map<String, String> updateReq = Map.of(
                "name", "Hacked Name",
                "email", otherCustomerUser.getEmail(),
                "password", "validPass123"
        );

        mockMvc.perform(put("/api/users/" + otherCustomerUser.getId())
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_OtherUser_Returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/users/" + otherCustomerUser.getId())
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_OtherUser_Returns403Forbidden() throws Exception {
        mockMvc.perform(delete("/api/users/" + otherCustomerUser.getId())
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserRole_AsAdmin_Success() throws Exception {
        Map<String, String> roleReq = Map.of("role", "RESTAURANT");

        mockMvc.perform(put("/api/admin/users/" + customerUser.getId() + "/role")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("RESTAURANT")));
    }

    @Test
    void updateUserRole_AsCustomer_Returns403Forbidden() throws Exception {
        Map<String, String> roleReq = Map.of("role", "ADMIN");

        mockMvc.perform(put("/api/admin/users/" + otherCustomerUser.getId() + "/role")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isForbidden());
    }
}
