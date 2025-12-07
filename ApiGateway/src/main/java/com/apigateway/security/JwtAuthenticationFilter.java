package com.apigateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
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

        // Public routes
        if (path.startsWith("/auth")
                || path.startsWith("/api/flight/search")
                || path.startsWith("/api/flight/getAllFlights")
                || path.startsWith("/api/flight/getFlight")
                || (path.startsWith("/api/flight/getAllAirlines") && method == HttpMethod.GET)) {

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

        String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        boolean isAdmin = normalizedRole.equals("ROLE_ADMIN");
        boolean isUser = normalizedRole.equals("ROLE_USER");

       
        if (path.startsWith("/api/booking") && !isUser) {
            return forbidden(exchange);
        }

        if (path.startsWith("/api/flight")) {
            if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.DELETE) {
                if (!isAdmin) {
                    return forbidden(exchange);
                }
            }
        }

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                claims.getSubject(), null,
                                List.of(new SimpleGrantedAuthority(normalizedRole))
                        )
                ));
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


