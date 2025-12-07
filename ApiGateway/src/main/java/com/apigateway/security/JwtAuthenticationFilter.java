package com.apigateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getPath().toString();
        HttpMethod method = exchange.getRequest().getMethod();

        System.out.println("🔍 Incoming Path = " + path);

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
        String role = claims.get("role", String.class);
        if (role == null) {
            return unauthorized(exchange);
        }

        String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(), null,
                                List.of(new SimpleGrantedAuthority(normalizedRole))
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

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
