package com.apigateway.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="users")
public record User(
        @Id String id,
        String username,
        String password,
        String fullName,
        String email,
        Role role,
        Instant createdAt
) { }
