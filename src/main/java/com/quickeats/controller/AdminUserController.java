package com.quickeats.controller;

import com.quickeats.dto.UserResponseDTO;
import com.quickeats.model.User;
import com.quickeats.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponseDTO> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> roleMap) {
        String role = roleMap.get("role");
        User updatedUser = userService.updateUserRole(id, role);
        return ResponseEntity.ok(UserResponseDTO.fromEntity(updatedUser));
    }
}
