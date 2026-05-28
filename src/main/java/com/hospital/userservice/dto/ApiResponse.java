package com.hospital.userservice.dto;

public record ApiResponse<T>(
    String status,
    String message,
    T data
) {}
