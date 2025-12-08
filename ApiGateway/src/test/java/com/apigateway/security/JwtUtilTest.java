package com.apigateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.apigateway.model.Role;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600_000L);
    }

    @Test
    void generateAndParseToken() {
        String token = jwtUtil.generateToken("alice", Role.ROLE_ADMIN);

        assertThat(jwtUtil.isValid(token)).isTrue();
        assertThat(jwtUtil.extractClaims(token).getSubject()).isEqualTo("alice");
        assertThat(jwtUtil.extractClaims(token).get("role", String.class)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void invalidTokenReturnsFalse() {
        assertThat(jwtUtil.isValid("not-a-token")).isFalse();
    }
}
