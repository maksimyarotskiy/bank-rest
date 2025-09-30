package com.bank.bank_rest.controller;

import com.bank.bank_rest.dto.login.LoginRequest;
import com.bank.bank_rest.dto.login.LoginResponse;
import com.bank.bank_rest.dto.user.UserRegistrationRequest;
import com.bank.bank_rest.dto.user.UserResponse;
import com.bank.bank_rest.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Registers a new user")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest userRequest) {
        UserResponse response = authService.registerUser(userRequest);
        return ResponseEntity.ok(response);
    }
}
