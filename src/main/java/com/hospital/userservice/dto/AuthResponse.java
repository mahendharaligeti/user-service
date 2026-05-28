package com.hospital.userservice.dto;

import com.hospital.userservice.entity.Role;

public record AuthResponse(
    String token,
    String email,
    Role role,
    String message
) {}
