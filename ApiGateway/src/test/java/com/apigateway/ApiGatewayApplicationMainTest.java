package com.apigateway;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class ApiGatewayApplicationMainTest {

    @Test
    void mainInvokesSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            ApiGatewayApplication.main(new String[]{"--spring.main.web-application-type=none"});
            mocked.verify(() -> SpringApplication.run(ApiGatewayApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }
}
