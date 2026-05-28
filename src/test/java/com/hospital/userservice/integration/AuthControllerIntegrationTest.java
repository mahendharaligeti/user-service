package com.hospital.userservice.integration;

import com.hospital.userservice.entity.Role;
import com.hospital.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegister_ValidRequest_Returns201() throws Exception {
        String requestBody = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john@example.com",
                "password": "password123",
                "role": "PATIENT"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data.email").value("john@example.com"))
            .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void testRegister_DuplicateEmail_Returns409() throws Exception {
        String requestBody = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john@example.com",
                "password": "password123",
                "role": "PATIENT"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value("error"))
            .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void testRegister_InvalidEmail_Returns400() throws Exception {
        String requestBody = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "invalid-email",
                "password": "password123",
                "role": "PATIENT"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void testLogin_ValidCredentials_ReturnsTokenAndRole() throws Exception {
        String registerBody = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john@example.com",
                "password": "password123",
                "role": "PATIENT"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
            .andExpect(status().isCreated());

        String loginBody = """
            {
                "email": "john@example.com",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data.email").value("john@example.com"))
            .andExpect(jsonPath("$.data.role").value("PATIENT"))
            .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void testLogin_WrongPassword_Returns401() throws Exception {
        String registerBody = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john@example.com",
                "password": "password123",
                "role": "PATIENT"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
            .andExpect(status().isCreated());

        String loginBody = """
            {
                "email": "john@example.com",
                "password": "wrongpassword"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value("error"));
    }
}
