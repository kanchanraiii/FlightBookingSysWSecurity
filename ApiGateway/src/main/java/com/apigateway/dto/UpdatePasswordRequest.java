package com.apigateway.dto;

public record UpdatePasswordRequest(
        String oldPassword,
        String newPassword
) {}
