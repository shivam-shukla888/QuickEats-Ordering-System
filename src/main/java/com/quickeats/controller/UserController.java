package com.quickeats.controller;

import com.quickeats.dto.UserResponseDTO;
import com.quickeats.exception.ResourceNotFoundException;
import com.quickeats.model.User;
import com.quickeats.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // NOTE: Login and Register endpoints have been consolidated into AuthController (/api/auth)
    // to avoid duplicate handler mappings and ensure consistent token response format.

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<UserResponseDTO> users = userService.getAllUsers(pageable)
                .map(UserResponseDTO::fromEntity);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        verifyUserOwnershipOrAdmin(id);
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        verifyUserOwnershipOrAdmin(id);
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        verifyUserOwnershipOrAdmin(id);
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PutMapping("/{id}/role")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> roleMap) {
        String role = roleMap.get("role");
        User updatedUser = userService.updateUserRole(id, role);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updatedUser));
    }

    private void verifyUserOwnershipOrAdmin(Long targetUserId) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: User is not authenticated");
        }
        User authUser = userService.getUserByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));

        boolean isAdmin = authUser.getRole() != null && "ADMIN".equalsIgnoreCase(authUser.getRole());
        boolean isOwner = authUser.getId() != null && authUser.getId().equals(targetUserId);

        if (!isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You can only modify your own account unless you are an ADMIN");
        }
    }
}
