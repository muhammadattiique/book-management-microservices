package com.bookstore.service;

import com.bookstore.dto.AuthRequest;
import com.bookstore.dto.UserResponse;
import com.bookstore.entity.User;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponse register(AuthRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hashing
                .role("ADMIN") // Set to ADMIN if you want to test admin endpoints right away
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .build();
    }

    public String login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Pass claims or role if your JwtService supports it, or generate token with username
        return jwtService.generateToken(user.getUsername());
    }
}