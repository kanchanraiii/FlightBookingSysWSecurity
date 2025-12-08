package com.apigateway.dto;

import com.apigateway.model.Role;

/**
 * Immutable registration payload to remove duplicated boilerplate with AuthLoginRequest.
 */
public record AuthRegisterRequest(
        String username,
        String password,
        String fullName,
        String email,
        Role role) {
}
