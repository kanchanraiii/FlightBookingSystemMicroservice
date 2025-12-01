package com.flightservice;

import com.flightservice.exceptions.GlobalErrorHandler;
import com.flightservice.exceptions.ResourceNotFoundException;
import com.flightservice.exceptions.ValidationException;
import com.flightservice.model.Cities;
import com.flightservice.model.TripType;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import reactor.test.StepVerifier;

import java.time.format.DateTimeParseException;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalErrorHandlerTests {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    @Test
    @DisplayName("Handles custom validation exceptions")
    void handleCustomValidation() {
        ValidationException ex = new ValidationException("Custom validation failed");

        StepVerifier.create(handler.handleCustomValidation(ex))
                .assertNext(map -> assertEquals("Custom validation failed", map.get("error")))
                .verifyComplete();
    }

    @Test
    @DisplayName("Handles resource not found")
    void handleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Item not found");

        StepVerifier.create(handler.handleNotFound(ex))
                .assertNext(map -> assertEquals("Item not found", map.get("error")))
                .verifyComplete();
    }

    @Test
    @DisplayName("Handles invalid city in JSON")
    void handleInvalidCity() {
        InvalidFormatException cause = new InvalidFormatException(null, "bad city", "X", Cities.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON error", cause, null);

        StepVerifier.create(handler.handleInvalidJson(ex))
                .assertNext(map -> assertTrue(map.get("error").contains("Invalid city")))
                .verifyComplete();
    }

    @Test
    @DisplayName("Handles invalid trip type")
    void handleInvalidTripType() {
        InvalidFormatException cause = new InvalidFormatException(null, "bad trip", "ROUND_TRPPP", TripType.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON error", cause, null);

        StepVerifier.create(handler.handleInvalidJson(ex))
                .assertNext(map -> assertTrue(map.get("error").contains("Invalid trip type")))
                .verifyComplete();
    }


    @Test
    @DisplayName("Handles invalid boolean value")
    void handleInvalidBoolean() {
        InvalidFormatException cause = new InvalidFormatException(null, "not_bool", "abc", Boolean.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON error", cause, null);

        StepVerifier.create(handler.handleInvalidJson(ex))
                .assertNext(map -> assertTrue(map.get("error").contains("Invalid boolean value")))
                .verifyComplete();
    }

    @Test
    @DisplayName("Handles invalid date format")
    void handleInvalidDate() {
        DateTimeParseException cause = new DateTimeParseException("Bad date", "2025-13-01", 5);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON error", cause, null);

        StepVerifier.create(handler.handleInvalidJson(ex))
                .assertNext(map -> assertEquals("Invalid date format. Use yyyy-MM-dd", map.get("error")))
                .verifyComplete();
    }

    @Test
    @DisplayName("Handles invalid JSON fallback")
    void handleInvalidJsonFallback() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("JSON error", new RuntimeException("Unknown"), null);

        StepVerifier.create(handler.handleInvalidJson(ex))
                .assertNext(map -> assertEquals("Invalid JSON request", map.get("error")))
                .verifyComplete();
    }

    @Test
    @DisplayName("Handles generic exception")
    void handleGeneric() {
        Exception ex = new Exception("Random failure");

        StepVerifier.create(handler.handleOthers(ex))
                .assertNext(map -> assertEquals("Random failure", map.get("error")))
                .verifyComplete();
    }

    @Test
    @DisplayName("Handles binding validation errors")
    void handleBindingErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "req");
        bindingResult.addError(new FieldError("req", "field", "must not be null"));

        StepVerifier.create(handler.handleValidationErrors(
                        new org.springframework.web.bind.support.WebExchangeBindException(null, bindingResult)))
                .assertNext(map -> assertEquals("must not be null", map.get("field")))
                .verifyComplete();
    }
}
