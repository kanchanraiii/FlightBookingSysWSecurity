package com.apigateway.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.apigateway.model.Role;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getPath().toString();
        HttpMethod method = exchange.getRequest().getMethod();

        // Public routes (no auth required)
        if (isPublic(path, method)) {
            return chain.filter(exchange);
        }

        // Auth header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token)) {
            return unauthorized(exchange);
        }

        // Extract claims
        Claims claims = jwtUtil.extractClaims(token);
        String roleClaim = claims.get("role", String.class);
        if (roleClaim == null) {
            return unauthorized(exchange);
        }

        Role resolvedRole;
        try {
            String normalizedRole = roleClaim.startsWith("ROLE_") ? roleClaim : "ROLE_" + roleClaim;
            resolvedRole = Role.valueOf(normalizedRole);
        } catch (IllegalArgumentException ex) {
            return unauthorized(exchange);
        }

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(), null,
                                List.of(new SimpleGrantedAuthority(resolvedRole.name()))
                        )
                ));
    }

    private boolean isPublic(String path, HttpMethod method) {
        if (path.startsWith("/auth") || path.startsWith("/api/auth")) {
            return true;
        }
        if (method == HttpMethod.GET &&
                (path.startsWith("/flight/api/flight/getAllAirlines")
                        || path.startsWith("/flight/api/flight/getAllFlights"))) {
            return true;
        }
        if (method == HttpMethod.POST && path.startsWith("/flight/api/flight/search")) {
            return true;
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
