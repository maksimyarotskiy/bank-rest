package com.bank.bank_rest.service;

import com.bank.bank_rest.dto.login.LoginRequest;
import com.bank.bank_rest.dto.login.LoginResponse;
import com.bank.bank_rest.dto.user.UserRegistrationRequest;
import com.bank.bank_rest.dto.user.UserResponse;
import com.bank.bank_rest.model.User;
import com.bank.bank_rest.repository.UserRepository;
import com.bank.bank_rest.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setEnabled(true);
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with username: {}", savedUser.getUsername());
        
        return mapToUserResponse(savedUser);
    }
    
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        if (!user.isEnabled()) {
            throw new RuntimeException("Account is disabled");
        }
        
        String token = jwtTokenProvider.generateToken(user);
        log.info("User logged in successfully: {}", user.getUsername());
        
        return new LoginResponse(token, "Bearer", user.getUsername(), user.getRole().name());
    }
    
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        response.setEnabled(user.isEnabled());
        return response;
    }
}
