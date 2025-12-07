package com.apigateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getPath().toString();

        // Public routes
        if (path.startsWith("/auth") || path.startsWith("/flight/search") || path.startsWith("/flight/getAllFlights")) {
            return chain.filter(exchange);
        }

        // Check for Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return unauthorized(exchange);

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token))
            return unauthorized(exchange);

        Claims claims = jwtUtil.extractClaims(token);
        String role = (String) claims.get("role");

        // ROLE-based route checks
        if (path.startsWith("/booking") && !role.equals("ROLE_USER"))
            return forbidden(exchange);

        if (path.startsWith("/flight") && !role.equals("ROLE_ADMIN"))
            return forbidden(exchange);

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
