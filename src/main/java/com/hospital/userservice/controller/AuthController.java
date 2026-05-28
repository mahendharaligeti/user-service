package com.hospital.userservice.controller;

import com.hospital.userservice.dto.ApiResponse;
import com.hospital.userservice.dto.AuthResponse;
import com.hospital.userservice.dto.LoginRequest;
import com.hospital.userservice.dto.RegisterRequest;
import com.hospital.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.registerUser(request);
        return new ResponseEntity<>(
            new ApiResponse<>("success", "User registered successfully", response),
            HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.loginUser(request);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Login successful", response)
        );
    }
}
