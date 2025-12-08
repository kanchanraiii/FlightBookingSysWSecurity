package com.apigateway.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void accessorsWork() {
        User user = new User("id-1", "alice", "secret", "Alice Doe", "alice@example.com", Role.ROLE_ADMIN);

        assertEquals("id-1", user.id());
        assertEquals("alice", user.username());
        assertEquals("secret", user.password());
        assertEquals("Alice Doe", user.fullName());
        assertEquals("alice@example.com", user.email());
        assertEquals(Role.ROLE_ADMIN, user.role());
    }
}
