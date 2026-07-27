package com.fintech.portfolio.service;

import com.fintech.portfolio.dto.AuthResponse;
import com.fintech.portfolio.dto.LoginRequest;
import com.fintech.portfolio.dto.RegisterRequest;
import com.fintech.portfolio.entity.User;
import com.fintech.portfolio.repository.UserRepository;
import com.fintech.portfolio.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateTokenForUser(saved.getId(), saved.getUsername());
        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .username(saved.getUsername())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String token = jwtTokenProvider.generateTokenForUser(user.getId(), user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
}
