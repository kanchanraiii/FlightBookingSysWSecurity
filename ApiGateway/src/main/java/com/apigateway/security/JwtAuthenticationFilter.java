package com.apigateway.security;

import java.util.List;

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

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

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

        Claims claims = jwtUtil.isValid(token) ? jwtUtil.extractClaims(token) : null;
        String roleClaim = claims != null ? claims.get("role", String.class) : null;
        if (roleClaim == null) {
            return unauthorized(exchange);
        }

        try {
            String normalizedRole = roleClaim.startsWith("ROLE_") ? roleClaim : "ROLE_" + roleClaim;
            Role resolvedRole = Role.valueOf(normalizedRole);
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    claims.getSubject(), null,
                                    List.of(new SimpleGrantedAuthority(resolvedRole.name()))
                            )
                    ));
        } catch (IllegalArgumentException ex) {
            return unauthorized(exchange);
        }
    }

    private boolean isPublic(String path, HttpMethod method) {
        boolean authPublic = path.equals("/auth/login") || path.equals("/auth/register");
        boolean passwordResetPublic = path.equals("/auth/password-reset/request") || path.equals("/auth/password-reset/confirm");
        return authPublic
                || passwordResetPublic
                || path.startsWith("/api/auth")
                || (method == HttpMethod.GET &&
                (path.startsWith("/flight/api/flight/getAllAirlines")
                        || path.startsWith("/flight/api/flight/getAllFlights")))
                || (method == HttpMethod.POST && path.startsWith("/flight/api/flight/search"));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
