package com.bookingservice.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.format.DateTimeParseException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

class GlobalErrorHandlerAdditionalTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    @Test
    void handlesBooleanFormatError() {
        HttpMessageNotReadableException ex = Mockito.mock(HttpMessageNotReadableException.class);
        Mockito.when(ex.getCause()).thenReturn(
                new com.fasterxml.jackson.databind.exc.InvalidFormatException(null, "bad", "true", Boolean.class));
        Map<String, String> response = handler.handleInvalidJson(ex).block();
        assertThat(response).containsEntry("error", "Invalid boolean value. Allowed values: true or false");
    }

    @Test
    void handlesDateParseError() {
        HttpMessageNotReadableException ex = Mockito.mock(HttpMessageNotReadableException.class);
        Mockito.when(ex.getCause()).thenReturn(new DateTimeParseException("bad", "2025-13-01", 0));
        Map<String, String> response = handler.handleInvalidJson(ex).block();
        assertThat(response).containsEntry("error", "Invalid date format. Use yyyy-MM-dd");
    }

    @Test
    void handlesResponseStatusWithCustomReason() {
        ResponseStatusException ex = new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "conflict reason");
        Map<String, String> response = handler.handleResponseStatus(ex).block().getBody();
        assertThat(response).containsEntry("error", "conflict reason");
    }
}
