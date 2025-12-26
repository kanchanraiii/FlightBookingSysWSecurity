package com.apigateway.security;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class TokenStore {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final Duration ttl;

    public TokenStore(ReactiveStringRedisTemplate redisTemplate, @Value("${jwt.expiration}") long expirationMs) {
        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofMillis(expirationMs);
    }

    public Mono<Boolean> storeToken(String token, String username) {
        return redisTemplate.opsForValue()
                .set(token, username, ttl)
                .defaultIfEmpty(false);
    }

    public Mono<Boolean> isTokenPresent(String token) {
        return redisTemplate.hasKey(token).defaultIfEmpty(false);
    }
}
