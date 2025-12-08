package com.configserver;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class ConfigServerFlightBookingApplicationMainTest {

    @Test
    void mainInvokesSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            ConfigServerFlightBookingApplication.main(new String[]{"--spring.main.web-application-type=none"});
            mocked.verify(() -> SpringApplication.run(ConfigServerFlightBookingApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }
}
