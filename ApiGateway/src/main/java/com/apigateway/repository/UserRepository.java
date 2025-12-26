package com.apigateway.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.apigateway.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Flux<User> findByUsername(String username);
    Mono<User> findFirstByUsernameOrderByCreatedAtDesc(String username);
    Mono<User> findById(String userId);
}
