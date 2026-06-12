package com.cinepass.auth_service.service;

import com.cinepass.auth_service.dto.*;
import com.cinepass.auth_service.entity.Role;
import com.cinepass.auth_service.entity.User;
import com.cinepass.auth_service.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Simulated cache for password reset tokens: Token -> Email
    private final Map<String, String> passwordResetTokens = new HashMap<>();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        // Map role string to enum
        Role role = Role.USER;
        if (request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role. Accepted roles: USER, THEATRE_OWNER, ADMIN");
            }
        }

        // Create new user, encrypting password with BCrypt
        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                role
        );

        userRepository.save(user);

        // Generate JWT
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Validate encrypted password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Generate JWT
        String token = jwtService.generateToken(user);

        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No account registered with this email address"));

        // Generate a mock reset token
        // In a real application, we would send this token via email link (e.g. /reset-password?token=XYZ)
        String resetToken = jwtService.generateToken(user);
        passwordResetTokens.put(resetToken, user.getEmail());

        System.out.println("[forgot-password] Password reset token generated for " + user.getEmail() + ": " + resetToken);
        
        return resetToken;
    }

    public void resetPassword(ResetPasswordRequest request) {
        // Validate token exists in local cache
        String email = passwordResetTokens.get(request.getToken());
        if (email == null) {
            throw new IllegalArgumentException("Invalid or expired password reset token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update password with BCrypt hash
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Remove token from cache after use
        passwordResetTokens.remove(request.getToken());
        
        System.out.println("[reset-password] Password successfully updated for user: " + email);
    }
}
