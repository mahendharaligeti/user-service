package com.hospital.userservice.dto;

import com.hospital.userservice.entity.Role;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    Role role,
    LocalDateTime createdAt
) {}
