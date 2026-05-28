package com.hospital.userservice.exception;

import com.hospital.userservice.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        LOGGER.warn("User already exists error message={}", ex.getMessage());
        return new ResponseEntity<>(
            new ApiResponse<>("error", ex.getMessage(), null),
            HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(UserNotFoundException ex) {
        LOGGER.warn("User not found error message={}", ex.getMessage());
        return new ResponseEntity<>(
            new ApiResponse<>("error", ex.getMessage(), null),
            HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        LOGGER.warn("Bad credentials error");
        return new ResponseEntity<>(
            new ApiResponse<>("error", "Invalid email or password", null),
            HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        LOGGER.warn("Access denied error message={}", ex.getMessage());
        return new ResponseEntity<>(
            new ApiResponse<>("error", "Access denied", null),
            HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        LOGGER.warn("Validation failed errors={}", errors);
        return new ResponseEntity<>(
            new ApiResponse<>("error", "Validation failed", errors),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        LOGGER.error("Unhandled application error", ex);
        return new ResponseEntity<>(
            new ApiResponse<>("error", "An unexpected error occurred", null),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
