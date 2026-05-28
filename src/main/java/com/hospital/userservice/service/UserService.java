package com.hospital.userservice.service;

import com.hospital.userservice.dto.AuthResponse;
import com.hospital.userservice.dto.LoginRequest;
import com.hospital.userservice.dto.RegisterRequest;
import com.hospital.userservice.dto.UserResponse;
import com.hospital.userservice.entity.Role;

import java.util.List;

public interface UserService {
    AuthResponse registerUser(RegisterRequest request);
    AuthResponse loginUser(LoginRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    List<UserResponse> getUsersByRole(Role role);
}
