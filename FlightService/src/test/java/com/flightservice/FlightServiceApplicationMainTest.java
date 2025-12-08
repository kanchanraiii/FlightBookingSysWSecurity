package com.flightservice;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class FlightServiceApplicationMainTest {

    @Test
    void mainInvokesSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            FlightServiceApplication.main(new String[]{"--spring.main.web-application-type=none"});
            mocked.verify(() -> SpringApplication.run(FlightServiceApplication.class, new String[]{"--spring.main.web-application-type=none"}));
        }
    }
}
