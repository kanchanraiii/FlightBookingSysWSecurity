package com.apigateway.dto;

public record PasswordResetRequest(
        String username,
        String email
) {}
