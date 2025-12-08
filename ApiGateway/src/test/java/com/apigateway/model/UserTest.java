package com.apigateway.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void settersAndGettersWork() {
        User user = new User();
        user.setId("id-1");
        user.setUsername("alice");
        user.setPassword("secret");
        user.setFullName("Alice Doe");
        user.setEmail("alice@example.com");
        user.setRole(Role.ROLE_ADMIN);

        assertEquals("id-1", user.getId());
        assertEquals("alice", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("Alice Doe", user.getFullName());
        assertEquals("alice@example.com", user.getEmail());
        assertEquals(Role.ROLE_ADMIN, user.getRole());
    }
}
