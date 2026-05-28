package com.hospital.userservice.controller;

import com.hospital.userservice.dto.ApiResponse;
import com.hospital.userservice.dto.UserResponse;
import com.hospital.userservice.entity.Role;
import com.hospital.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        LOGGER.debug("Get all users request received");
        List<UserResponse> users = userService.getAllUsers();
        LOGGER.info("Get all users request completed count={}", users.size());
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Users retrieved successfully", users)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id, Authentication authentication) {
        LOGGER.debug("Get user by id request received userId={}", id);
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(auth -> auth.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            LOGGER.warn("Get user by id denied userId={}", id);
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        UserResponse user = userService.getUserById(id);
        LOGGER.info("Get user by id request completed userId={}", id);
        return ResponseEntity.ok(
            new ApiResponse<>("success", "User retrieved successfully", user)
        );
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable Role role) {
        LOGGER.debug("Get users by role request received role={}", role);
        List<UserResponse> users = userService.getUsersByRole(role);
        LOGGER.info("Get users by role request completed role={} count={}", role, users.size());
        return ResponseEntity.ok(
            new ApiResponse<>("success", "Users retrieved successfully", users)
        );
    }
}
