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
}
