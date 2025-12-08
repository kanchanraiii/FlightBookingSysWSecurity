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

import com.apigateway.dto.AuthLoginRequest;
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

        verify(userRepository).save(argThat(saved -> !saved.getPassword().equals("password")));
    }

    @Test
    void loginSucceedsAndReturnsToken() {
        String encoded = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("password");
        User stored = buildUser("1", "alice", encoded, Role.ROLE_USER);
        when(userRepository.findByUsername("alice")).thenReturn(Mono.just(stored));
        when(jwtUtil.generateToken("alice", Role.ROLE_USER)).thenReturn("token123");

        StepVerifier.create(controller.login(buildLogin("alice", "password")))
                .expectNext("token123")
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
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPassword(password);
        u.setRole(role);
        return u;
    }

    private AuthRegisterRequest buildRegister(String username, String password, Role role) {
        AuthRegisterRequest req = new AuthRegisterRequest();
        req.setUsername(username);
        req.setPassword(password);
        req.setRole(role);
        return req;
    }

    private AuthLoginRequest buildLogin(String username, String password) {
        AuthLoginRequest req = new AuthLoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        return req;
    }
}
