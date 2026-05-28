package com.hospital.userservice.unit;

import com.hospital.userservice.config.CustomUserDetailsService;
import com.hospital.userservice.dto.AuthResponse;
import com.hospital.userservice.dto.LoginRequest;
import com.hospital.userservice.dto.RegisterRequest;
import com.hospital.userservice.entity.Role;
import com.hospital.userservice.entity.User;
import com.hospital.userservice.exception.UserAlreadyExistsException;
import com.hospital.userservice.exception.UserNotFoundException;
import com.hospital.userservice.repository.UserRepository;
import com.hospital.userservice.service.UserServiceImpl;
import com.hospital.userservice.util.JwtUtil;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, jwtUtil,
            authenticationManager, userDetailsService, ObservationRegistry.NOOP);
    }

    @Test
    void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "password123", Role.PATIENT);

        User user = new User("John", "Doe", "john@example.com", "hashedPassword", Role.PATIENT);
        user.setId(1L);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userDetailsService.loadUserByUsername(request.email())).thenReturn(mock(UserDetails.class));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = userService.registerUser(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.role()).isEqualTo(Role.PATIENT);
        assertThat(response.token()).isEqualTo("jwt-token");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "password123", Role.PATIENT);

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(request))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("Email already registered");
    }

    @Test
    void testLoginUser_Success() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");

        User user = new User("John", "Doe", "john@example.com", "hashedPassword", Role.PATIENT);
        user.setId(1L);

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        AuthResponse response = userService.loginUser(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void testLoginUser_InvalidPassword_ThrowsException() {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> userService.loginUser(request))
            .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);
    }

    @Test
    void testGetUserById_Found() {
        User user = new User("John", "Doe", "john@example.com", "hashedPassword", Role.PATIENT);
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("john@example.com");
    }

    @Test
    void testGetUserById_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found");
    }
}
