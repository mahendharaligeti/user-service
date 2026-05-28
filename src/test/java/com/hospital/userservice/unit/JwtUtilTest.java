package com.hospital.userservice.unit;

import com.hospital.userservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "a-256-bit-secret-key-for-hospital-user-service-must-be-256-bits");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 86400000L);
    }

    @Test
    void testGenerateToken_NotNull() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            "john@example.com",
            "password",
            Collections.singleton(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return "ROLE_PATIENT";
                }
            })
        );

        String token = jwtUtil.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void testExtractUsername_MatchesEmail() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            "john@example.com",
            "password",
            Collections.singleton(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return "ROLE_PATIENT";
                }
            })
        );

        String token = jwtUtil.generateToken(userDetails);
        String extractedUsername = jwtUtil.extractUsername(token);

        assertThat(extractedUsername).isEqualTo("john@example.com");
    }

    @Test
    void testIsTokenValid_ValidToken_ReturnsTrue() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            "john@example.com",
            "password",
            Collections.singleton(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return "ROLE_PATIENT";
                }
            })
        );

        String token = jwtUtil.generateToken(userDetails);
        boolean isValid = jwtUtil.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void testIsTokenValid_InvalidToken_ReturnsFalse() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            "john@example.com",
            "password",
            Collections.singleton(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return "ROLE_PATIENT";
                }
            })
        );

        boolean isValid = jwtUtil.isTokenValid("invalid-token", userDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    void testExtractRole_MatchesRole() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            "john@example.com",
            "password",
            Collections.singleton(new GrantedAuthority() {
                @Override
                public String getAuthority() {
                    return "ROLE_PATIENT";
                }
            })
        );

        String token = jwtUtil.generateToken(userDetails);
        String extractedRole = jwtUtil.extractRole(token);

        assertThat(extractedRole).isEqualTo("ROLE_PATIENT");
    }
}
