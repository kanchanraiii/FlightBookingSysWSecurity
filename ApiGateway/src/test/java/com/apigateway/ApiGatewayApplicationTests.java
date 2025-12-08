package com.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.apigateway.repository.UserRepository;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration",
                "jwt.secret=01234567890123456789012345678901",
                "jwt.expiration=3600000",
                "spring.main.web-application-type=reactive",
                "spring.data.mongodb.repositories.type=none",
                "spring.cloud.gateway.enabled=false"
        }
)
@Import(ApiGatewayApplicationTests.TestSecurityConfig.class)
class ApiGatewayApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        ServerHttpSecurity serverHttpSecurity() {
            return ServerHttpSecurity.http();
        }

        @Bean
        ServerProperties serverProperties() {
            return new ServerProperties();
        }
    }

    @Test
    void contextLoads() {
        // Context bootstrap only; no web server or external deps.
    }
}
