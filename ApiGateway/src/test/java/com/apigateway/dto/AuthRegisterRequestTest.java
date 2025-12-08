package com.apigateway.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.apigateway.model.Role;

class AuthRegisterRequestTest {

    @Test
    void accessorsCoverAllFields() {
        AuthRegisterRequest req = new AuthRegisterRequest(
                "user",
                "pass",
                "Full Name",
                "user@example.com",
                Role.ROLE_USER);

        assertEquals("user", req.username());
        assertEquals("pass", req.password());
        assertEquals("Full Name", req.fullName());
        assertEquals("user@example.com", req.email());
        assertEquals(Role.ROLE_USER, req.role());
    }
}
