package com.hospital.userservice.smoke;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.hospital.userservice.controller.AuthController;
import com.hospital.userservice.controller.UserController;
import com.hospital.userservice.repository.UserRepository;
import com.hospital.userservice.service.UserService;
import com.hospital.userservice.util.JwtUtil;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SmokeTest {

    @Autowired
    private AuthController authController;

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void contextLoads() {
        assertThat(authController).isNotNull();
    }

    @Test
    void userRepositoryNotNull() {
        assertThat(userRepository).isNotNull();
    }

    @Test
    void userServiceNotNull() {
        assertThat(userService).isNotNull();
    }

    @Test
    void jwtUtilNotNull() {
        assertThat(jwtUtil).isNotNull();
    }

    @Test
    void authControllerNotNull() {
        assertThat(authController).isNotNull();
    }

    @Test
    void userControllerNotNull() {
        assertThat(userController).isNotNull();
    }
}
