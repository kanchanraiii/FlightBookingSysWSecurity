package com.apigateway.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;

import com.apigateway.dto.AuthLoginRequest;
import com.apigateway.dto.AuthLoginResponse;
import com.apigateway.dto.AuthRegisterRequest;
import com.apigateway.model.Role;
import com.apigateway.model.User;
import com.apigateway.repository.UserRepository;
import com.apigateway.security.JwtUtil;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController controller;

    private final AuthRegisterRequest sampleUser = buildRegister("alice", "password", Role.ROLE_USER);

    @BeforeEach
    void configureSecret() {
        ReflectionTestUtils.setField(jwtUtil, "secret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600_000L);
    }

    @Test
    void registerEncodesPasswordAndPersists() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0, User.class)));

        StepVerifier.create(controller.register(sampleUser))
                .expectNext("User registered successfully")
                .verifyComplete();

        verify(userRepository).save(argThat(saved -> !saved.password().equals("password")
                && saved.createdAt() != null));
    }

    @Test
    void loginSucceedsAndReturnsToken() {
        String encoded = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("password");
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        User stored = new User("1", "alice", encoded, null, null, Role.ROLE_USER, createdAt);
        when(userRepository.findByUsername("alice")).thenReturn(Mono.just(stored));
        when(jwtUtil.generateToken("alice", Role.ROLE_USER)).thenReturn("token123");

        StepVerifier.create(controller.login(buildLogin("alice", "password")))
                .expectNextMatches(resp -> {
                    assertThat(resp).isInstanceOf(AuthLoginResponse.class);
                    assertThat(resp.token()).isEqualTo("token123");
                    assertThat(resp.createdAt()).isEqualTo(createdAt);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void loginFailsForInvalidPassword() {
        String encoded = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("password");
        User stored = buildUser("1", "alice", encoded, Role.ROLE_USER);
        when(userRepository.findByUsername("alice")).thenReturn(Mono.just(stored));

        StepVerifier.create(controller.login(buildLogin("alice", "wrong")))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) err).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                }).verify();
    }

    @Test
    void loginFailsWhenUserMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Mono.empty());

        StepVerifier.create(controller.login(buildLogin("ghost", "password")))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    private User buildUser(String id, String username, String password, Role role) {
        return new User(id, username, password, null, null, role, Instant.parse("2024-01-01T00:00:00Z"));
    }

    private AuthRegisterRequest buildRegister(String username, String password, Role role) {
        return new AuthRegisterRequest(username, password, null, null, role);
    }

    private AuthLoginRequest buildLogin(String username, String password) {
        return new AuthLoginRequest(username, password);
    }
}
