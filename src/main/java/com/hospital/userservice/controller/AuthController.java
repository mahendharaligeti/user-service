package com.hospital.userservice.controller;

import com.hospital.userservice.dto.ApiResponse;
import com.hospital.userservice.dto.AuthResponse;
import com.hospital.userservice.dto.LoginRequest;
import com.hospital.userservice.dto.RegisterRequest;
import com.hospital.userservice.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        LOGGER.info("Register request received for email={} role={}", request.email(), request.role());
        AuthResponse response = userService.registerUser(request);
        LOGGER.info("Register request completed for email={} role={}", response.email(), response.role());
        return new ResponseEntity<>(
            new ApiResponse<>("success", "User registered successfully", response),
            HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        LOGGER.info("Login request received for email={}", request.email());
        AuthResponse response = userService.loginUser(request);
        LOGGER.info("Login request completed for email={} role={}", response.email(), response.role());
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Login successful", response)
        );
    }
}
