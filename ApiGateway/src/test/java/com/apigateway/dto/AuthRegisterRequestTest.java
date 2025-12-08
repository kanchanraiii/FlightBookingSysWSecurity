package com.apigateway.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.apigateway.model.Role;

class AuthRegisterRequestTest {

    @Test
    void settersAndGettersCoverInheritance() {
        AuthRegisterRequest req = new AuthRegisterRequest();
        req.setUsername("user");
        req.setPassword("pass");
        req.setFullName("Full Name");
        req.setEmail("user@example.com");
        req.setRole(Role.ROLE_USER);

        assertEquals("user", req.getUsername());
        assertEquals("pass", req.getPassword());
        assertEquals("Full Name", req.getFullName());
        assertEquals("user@example.com", req.getEmail());
        assertEquals(Role.ROLE_USER, req.getRole());
    }
}
