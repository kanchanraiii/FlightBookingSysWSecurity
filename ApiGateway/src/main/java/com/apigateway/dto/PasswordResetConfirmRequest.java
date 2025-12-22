package com.apigateway.dto;

public record PasswordResetConfirmRequest(
        String username,
        String code,
        String newPassword
) {}
