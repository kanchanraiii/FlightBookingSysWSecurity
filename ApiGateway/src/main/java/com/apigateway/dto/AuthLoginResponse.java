package com.apigateway.dto;

import java.time.Instant;

public record AuthLoginResponse(
        String token,
        Instant createdAt
) { }
