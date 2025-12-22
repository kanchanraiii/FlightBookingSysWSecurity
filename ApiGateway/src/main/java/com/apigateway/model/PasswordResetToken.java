package com.apigateway.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "password_reset_tokens")
public record PasswordResetToken(
        @Id String id,
        String username,
        String code,
        Instant expiresAt,
        boolean used
) {
}
