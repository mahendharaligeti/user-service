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
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.function.Supplier;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ObservationRegistry observationRegistry;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil, AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService, ObservationRegistry observationRegistry) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public AuthResponse registerUser(RegisterRequest request) {
        LOGGER.debug("Checking whether user email is already registered email={}", request.email());
        if (observeDatabaseCall("db.user.exists-by-email", () -> userRepository.existsByEmail(request.email()))) {
            LOGGER.warn("User registration rejected because email already exists email={}", request.email());
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = new User(
            request.firstName(),
            request.lastName(),
            request.email(),
            passwordEncoder.encode(request.password()),
            request.role()
        );

        User savedUser = observeDatabaseCall("db.user.save", () -> userRepository.save(user));
        LOGGER.info("User registered successfully userId={} email={} role={}",
            savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

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
            LOGGER.debug("Authenticating user email={}", request.email());
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            User user = observeDatabaseCall("db.user.find-by-email", () -> userRepository.findByEmail(request.email()))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
            LOGGER.info("User login successful userId={} email={} role={}",
                user.getId(), user.getEmail(), user.getRole());

            return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole(),
                "Login successful"
            );
        } catch (Exception e) {
            LOGGER.warn("User login failed email={} reason={}", request.email(), e.getClass().getSimpleName());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = observeDatabaseCall("db.user.find-by-id", () -> userRepository.findById(id))
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        LOGGER.debug("User found userId={} role={}", user.getId(), user.getRole());
        return convertToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = observeDatabaseCall("db.user.find-all", userRepository::findAll);
        LOGGER.debug("Retrieved all users count={}", users.size());
        return users.stream()
            .map(this::convertToResponse)
            .toList();
    }

    @Override
    public List<UserResponse> getUsersByRole(Role role) {
        List<User> users = observeDatabaseCall("db.user.find-all-for-role", userRepository::findAll);
        return users.stream()
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

    private <T> T observeDatabaseCall(String name, Supplier<T> supplier) {
        return Observation.createNotStarted(name, observationRegistry)
            .lowCardinalityKeyValue("component", "database")
            .lowCardinalityKeyValue("db.system", "mysql")
            .observe(supplier);
    }
}
