package com.quickeats.service;

import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.User;
import com.quickeats.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("John Doe", "john@example.com", "password123", "CUSTOMER");
        sampleUser.setId(1L);
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail(sampleUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(sampleUser);

        assertNotNull(created);
        assertEquals("john@example.com", created.getEmail());
        assertEquals("encodedPassword", created.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByEmail(sampleUser.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(sampleUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        Optional<User> found = userService.getUserById(1L);

        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
    }

    @Test
    void createUser_PrivilegeEscalationAttempt_AlwaysSetsCustomerRole() {
        User maliciousUser = new User("Attacker", "attacker@example.com", "pass123", "ADMIN");
        when(userRepository.existsByEmail(maliciousUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(maliciousUser);

        assertNotNull(created);
        assertEquals("CUSTOMER", created.getRole(), "Role must be forced to CUSTOMER during creation");
    }

    @Test
    void updateUserRole_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUserRole(1L, "ADMIN");

        assertNotNull(updated);
        assertEquals("ADMIN", updated.getRole());
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
    }
}
