package com.apigateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.WebFilterChain;

import com.apigateway.model.Role;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600_000L);
        filter = new JwtAuthenticationFilter(jwtUtil);
    }

    @Test
    void publicRoutesBypassAuthentication() {
        AtomicBoolean called = new AtomicBoolean(false);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/auth/login").build());
        WebFilterChain chain = e -> {
            called.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(called).isTrue();
    }

    @Test
    void missingHeaderReturnsUnauthorized() {
        AtomicBoolean called = new AtomicBoolean(false);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secure").build());
        WebFilterChain chain = e -> {
            called.set(true);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(called).isFalse();
    }

    @Test
    void invalidTokenReturnsUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secure")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer broken").build());
        WebFilterChain chain = e -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void validTokenPopulatesSecurityContext() {
        String token = jwtUtil.generateToken("bob", Role.ROLE_ADMIN);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secure").build());
        exchange.getRequest().mutate().header(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        AtomicReference<Authentication> authRef = new AtomicReference<>();
        WebFilterChain chain = e -> ReactiveSecurityContextHolder.getContext()
                .doOnNext(ctx -> authRef.set(ctx.getAuthentication()))
                .then();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(authRef.get()).isNotNull();
        assertThat(authRef.get().getAuthorities()).extracting("authority").contains("ROLE_ADMIN");
    }

    @Test
    void publicFlightEndpointsBypassAuth() {
        MockServerWebExchange getExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/flight/api/flight/getAllFlights").build());
        MockServerWebExchange postExchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/flight/api/flight/search").build());
        AtomicBoolean getCalled = new AtomicBoolean(false);
        AtomicBoolean postCalled = new AtomicBoolean(false);
        WebFilterChain chain = e -> {
            if ("/flight/api/flight/getAllFlights".equals(e.getRequest().getPath().toString())) {
                getCalled.set(true);
            } else if ("/flight/api/flight/search".equals(e.getRequest().getPath().toString())) {
                postCalled.set(true);
            }
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(getExchange, chain)).verifyComplete();
        StepVerifier.create(filter.filter(postExchange, chain)).verifyComplete();
        assertThat(getCalled).isTrue();
        assertThat(postCalled).isTrue();
    }

    @Test
    void tokenWithoutRoleClaimIsUnauthorized() {
        String token = io.jsonwebtoken.Jwts.builder()
                .setSubject("bob")
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 3600_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes()), io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secure")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build());

        StepVerifier.create(filter.filter(exchange, e -> Mono.empty())).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void tokenWithInvalidRoleIsUnauthorized() {
        String token = io.jsonwebtoken.Jwts.builder()
                .setSubject("bob")
                .claim("role", "NOT_A_ROLE")
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 3600_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes()), io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secure")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build());

        StepVerifier.create(filter.filter(exchange, e -> Mono.empty())).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void tokenWithRoleWithoutPrefixIsAccepted() {
        String token = io.jsonwebtoken.Jwts.builder()
                .setSubject("alice")
                .claim("role", "ADMIN") // should be normalized to ROLE_ADMIN
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 3600_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes()), io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secure").build());
        exchange.getRequest().mutate().header(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        AtomicReference<Authentication> authRef = new AtomicReference<>();
        WebFilterChain chain = e -> ReactiveSecurityContextHolder.getContext()
                .doOnNext(ctx -> authRef.set(ctx.getAuthentication()))
                .then();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(authRef.get()).isNotNull();
        assertThat(authRef.get().getAuthorities()).extracting("authority").contains("ROLE_ADMIN");
    }
}
