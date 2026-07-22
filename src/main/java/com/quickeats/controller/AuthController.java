package com.quickeats.controller;

import com.quickeats.dto.AuthRequestDTO;
import com.quickeats.dto.AuthResponseDTO;
import com.quickeats.dto.UserResponseDTO;
import com.quickeats.model.RefreshToken;
import com.quickeats.model.User;
import com.quickeats.security.JwtUtil;
import com.quickeats.service.RefreshTokenService;
import com.quickeats.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody User user) {
        User createdUser = userService.createUser(user);
        String accessToken = jwtUtil.generateToken(createdUser.getEmail(), createdUser.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(createdUser.getId());

        AuthResponseDTO response = new AuthResponseDTO(accessToken, refreshToken.getToken(), UserResponseDTO.fromEntity(createdUser));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO loginRequest) {
        User user = userService.getUserByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        AuthResponseDTO response = new AuthResponseDTO(accessToken, refreshToken.getToken(), UserResponseDTO.fromEntity(user));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshAccessToken(@RequestBody Map<String, String> request) {
        String requestRefreshToken = request.get("refreshToken");

        if (requestRefreshToken == null || requestRefreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh Token is required!"));
        }

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
                    RefreshToken rotatedRefreshToken = refreshTokenService.createRefreshToken(user.getId());
                    return ResponseEntity.ok(Map.of(
                            "token", newAccessToken,
                            "accessToken", newAccessToken,
                            "refreshToken", rotatedRefreshToken.getToken(),
                            "user", UserResponseDTO.fromEntity(user)
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Refresh token is not in database or has been invalidated!")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody(required = false) Map<String, String> request) {
        if (request != null && request.containsKey("refreshToken")) {
            refreshTokenService.deleteByToken(request.get("refreshToken"));
        }
        return ResponseEntity.ok(Map.of("message", "User logged out successfully and refresh token invalidated."));
    }
}
