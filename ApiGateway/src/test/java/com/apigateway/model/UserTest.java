package com.apigateway.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import java.time.Instant;

class UserTest {

    @Test
    void accessorsWork() {
        Instant created = Instant.parse("2024-01-01T00:00:00Z");
        User user = new User("id-1", "alice", "secret", "Alice Doe", "alice@example.com", Role.ROLE_ADMIN, created);

        assertEquals("id-1", user.id());
        assertEquals("alice", user.username());
        assertEquals("secret", user.password());
        assertEquals("Alice Doe", user.fullName());
        assertEquals("alice@example.com", user.email());
        assertEquals(Role.ROLE_ADMIN, user.role());
        assertEquals(created, user.createdAt());
    }
}
