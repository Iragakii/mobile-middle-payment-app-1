package com.example.Service;

import com.example.Auth.JwtUtil;
import com.example.Entity.UserEntity;
import com.example.Model.AuthResponse;
import com.example.Model.ForgotPasswordRequest;
import com.example.Model.LoginRequest;
import com.example.Model.SignUpRequest;
import com.example.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse signUp(SignUpRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse("Email already exists");
        }

        // Create new user
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setIsActive(true);

        user = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            return new AuthResponse("Invalid username or password");
        }

        UserEntity user = userOpt.get();

        // Check if user is active
        if (!user.getIsActive()) {
            return new AuthResponse("Account is deactivated");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthResponse("Invalid username or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            // Don't reveal if email exists for security
            return new AuthResponse("If the email exists, a password reset link has been sent");
        }

        UserEntity user = userOpt.get();

        // In a real application, you would:
        // 1. Generate a password reset token
        // 2. Save it to the database with expiration
        // 3. Send an email with the reset link
        // For now, we'll just return a success message

        return new AuthResponse("If the email exists, a password reset link has been sent");
    }
}

