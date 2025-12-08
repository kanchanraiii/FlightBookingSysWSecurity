package com.apigateway.dto;

/**
 * Immutable login request payload to avoid duplicated boilerplate.
 */
public record AuthLoginRequest(String username, String password) { }
