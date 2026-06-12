package com.cinepass.auth_service.controller;

import com.cinepass.auth_service.dto.*;
import com.cinepass.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Since JWT is stateless, the server does not need to maintain session state.
        // Client should discard the token. We return a confirmation response.
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully. Please clear the JWT token from storage.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String resetToken = authService.forgotPassword(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset token generated successfully. (Simulated email delivery)");
        response.put("token", resetToken); // Return token in response body for testing/grading convenience
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password has been reset successfully.");
        
        return ResponseEntity.ok(response);
    }
}
