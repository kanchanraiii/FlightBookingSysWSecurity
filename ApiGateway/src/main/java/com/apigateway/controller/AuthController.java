package com.apigateway.controller;

import com.apigateway.model.User;
import com.apigateway.repository.UserRepository;
import com.apigateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> register(@RequestBody User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user).thenReturn("User registered successfully");
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> login(@RequestBody User loginRequest) {
        return repo.findByUsername(loginRequest.getUsername())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .flatMap(user -> {
                    if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
                    }
                    String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                    return Mono.just(token);
                });
    }
}
