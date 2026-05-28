package com.hospital.userservice.service;

import com.hospital.userservice.dto.AuthResponse;
import com.hospital.userservice.dto.LoginRequest;
import com.hospital.userservice.dto.RegisterRequest;
import com.hospital.userservice.dto.UserResponse;
import com.hospital.userservice.entity.Role;
import com.hospital.userservice.entity.User;
import com.hospital.userservice.exception.UserAlreadyExistsException;
import com.hospital.userservice.exception.UserNotFoundException;
import com.hospital.userservice.repository.UserRepository;
import com.hospital.userservice.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil, AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = new User(
            request.firstName(),
            request.lastName(),
            request.email(),
            passwordEncoder.encode(request.password()),
            request.role()
        );

        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return new AuthResponse(
            token,
            savedUser.getEmail(),
            savedUser.getRole(),
            "User registered successfully"
        );
    }

    @Override
    public AuthResponse loginUser(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

            return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole(),
                "Login successful"
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        return convertToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::convertToResponse)
            .toList();
    }

    @Override
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findAll().stream()
            .filter(user -> user.getRole() == role)
            .map(this::convertToResponse)
            .toList();
    }

    private UserResponse convertToResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
}
