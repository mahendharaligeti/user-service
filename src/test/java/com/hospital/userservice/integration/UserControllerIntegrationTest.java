package com.hospital.userservice.integration;

import com.hospital.userservice.entity.Role;
import com.hospital.userservice.entity.User;
import com.hospital.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String patientToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        User admin = new User("Admin", "User", "admin@example.com", passwordEncoder.encode("password123"), Role.ADMIN);
        User patient = new User("Patient", "User", "patient@example.com", passwordEncoder.encode("password123"), Role.PATIENT);

        userRepository.save(admin);
        userRepository.save(patient);

        adminToken = getToken("admin@example.com", "password123");
        patientToken = getToken("patient@example.com", "password123");
    }

    private String getToken(String email, String password) throws Exception {
        String loginBody = String.format("""
            {
                "email": "%s",
                "password": "%s"
            }
            """, email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.path("data").path("token").asText();
    }

    @Test
    void testGetAllUsers_AsAdmin_Returns200() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetAllUsers_AsPatient_Returns403() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + patientToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserById_AsAdmin_Returns200() throws Exception {
        User user = userRepository.findAll().get(0);

        mockMvc.perform(get("/api/users/" + user.getId())
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    void testGetUserById_WithoutAuth_Returns403() throws Exception {
        User user = userRepository.findAll().get(0);

        mockMvc.perform(get("/api/users/" + user.getId()))
            .andExpect(status().isForbidden());
    }

    @Test
    void testGetUserByRole_AsAdmin_Returns200() throws Exception {
        mockMvc.perform(get("/api/users/role/" + Role.PATIENT)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetUserByRole_AsDoctor_Returns200() throws Exception {
        User doctor = new User("Doctor", "User", "doctor@example.com", passwordEncoder.encode("password123"), Role.DOCTOR);
        userRepository.save(doctor);

        String doctorToken = getToken("doctor@example.com", "password123");

        mockMvc.perform(get("/api/users/role/" + Role.PATIENT)
                .header("Authorization", "Bearer " + doctorToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void testGetUserByRole_AsPatient_Returns403() throws Exception {
        mockMvc.perform(get("/api/users/role/" + Role.PATIENT)
                .header("Authorization", "Bearer " + patientToken))
            .andExpect(status().isForbidden());
    }
}
