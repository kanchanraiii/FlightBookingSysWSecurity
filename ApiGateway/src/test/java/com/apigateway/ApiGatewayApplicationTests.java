package com.apigateway;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.apigateway.model.Role;
import com.apigateway.security.JwtUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.config.import=optional:classpath:/",
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false",
                "spring.main.web-application-type=reactive",
                "spring.cloud.gateway.routes[0].id=dummy",
                "spring.cloud.gateway.routes[0].uri=http://example.org",
                "spring.cloud.gateway.routes[0].predicates[0]=Path=/dummy/**",
                "jwt.secret=testsecrettestsecrettestsecret12",
                "jwt.expiration=3600000"
        }
)
class GatewayRouteTest {

    private static final String TEST_SECRET = "testsecrettestsecrettestsecret12";

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    JwtUtil jwtUtil;

    @Test
    void contextLoads() {
        assertNotNull(webTestClient);
    }

    @Test
    void flightRouteMatched() {
        webTestClient.get()
                .uri("/flight/test")
                .header(HttpHeaders.AUTHORIZATION, bearer(Role.ROLE_USER))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void bookingRouteMatched() {
        webTestClient.post()
                .uri("/booking/test")
                .header(HttpHeaders.AUTHORIZATION, bearer(Role.ROLE_USER))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void validUserToken_allowsProtectedPath() {
        webTestClient.get()
                .uri("/booking/api/booking/ticket/PNR001")
                .header(HttpHeaders.AUTHORIZATION, bearer(Role.ROLE_USER))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void unknownRole_rejected() {
        webTestClient.get()
                .uri("/booking/api/booking/ticket/PNR002")
                .header(HttpHeaders.AUTHORIZATION, bearerWithCustomRole("ROLE_EVIL", new Date(System.currentTimeMillis() + 60_000)))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void expiredToken_rejected() {
        webTestClient.get()
                .uri("/booking/api/booking/ticket/PNR003")
                .header(HttpHeaders.AUTHORIZATION, bearerWithCustomRole(Role.ROLE_USER.name(), new Date(System.currentTimeMillis() - 5_000)))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private String bearer(Role role) {
        return "Bearer " + jwtUtil.generateToken("user", role);
    }

    private String bearerWithCustomRole(String role, Date exp) {
        Key key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setSubject("user")
                .claim("role", role)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return "Bearer " + token;
    }
}
