package com.bookingservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.bookingservice.exceptions.GlobalErrorHandler;
import com.bookingservice.exceptions.ResourceNotFoundException;
import com.bookingservice.exceptions.ValidationException;
import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.Gender;
import com.bookingservice.model.Meal;
import com.bookingservice.model.TripType;

import reactor.core.publisher.Mono;

class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    @Test
    void handlesValidationException() {
        Mono<Map<String, String>> result = handler.handleValidationException(new ValidationException("bad"));
        assertEquals("bad", result.block().get("error"));
    }

    @Test
    void handlesNotFound() {
        Mono<Map<String, String>> result = handler.handleNotFound(new ResourceNotFoundException("missing"));
        assertEquals("missing", result.block().get("error"));
    }

    @Test
    void handlesInvalidEnum() {
        com.fasterxml.jackson.databind.exc.InvalidFormatException ex =
                new com.fasterxml.jackson.databind.exc.InvalidFormatException(
                        null, "bad", "BAD", TripType.class);
        HttpMessageNotReadableException http = new HttpMessageNotReadableException("bad", ex, null);
        Mono<Map<String, String>> result = handler.handleInvalidJson(http);
        String msg = result.block().get("error");
        org.junit.jupiter.api.Assertions.assertTrue(msg.contains("Invalid trip type"));
    }

    @Test
    void handlesInvalidGenderEnum() {
        com.fasterxml.jackson.databind.exc.InvalidFormatException ex =
                new com.fasterxml.jackson.databind.exc.InvalidFormatException(
                        null, "bad", "BAD", Gender.class);
        HttpMessageNotReadableException http = new HttpMessageNotReadableException("bad", ex, null);
        Mono<Map<String, String>> result = handler.handleInvalidJson(http);
        String msg = result.block().get("error");
        org.junit.jupiter.api.Assertions.assertTrue(msg.contains("Invalid gender"));
    }

    @Test
    void handlesInvalidBoolean() {
        com.fasterxml.jackson.databind.exc.InvalidFormatException ex =
                new com.fasterxml.jackson.databind.exc.InvalidFormatException(
                        null, "bad", "BAD", Boolean.class);
        HttpMessageNotReadableException http = new HttpMessageNotReadableException("bad", ex, null);
        Mono<Map<String, String>> result = handler.handleInvalidJson(http);
        String msg = result.block().get("error");
        org.junit.jupiter.api.Assertions.assertTrue(msg.contains("Invalid boolean value"));
    }

    @Test
    void handlesGenericException() {
        Mono<Map<String, String>> result = handler.handleOthers(new RuntimeException("oops"));
        assertEquals("oops", result.block().get("error"));
    }

    @Test
    void handlesInvalidMealEnum() {
        var ex = new com.fasterxml.jackson.databind.exc.InvalidFormatException(
                null, "bad", "BAD", Meal.class);
        HttpMessageNotReadableException http = new HttpMessageNotReadableException("bad", ex, null);
        String msg = handler.handleInvalidJson(http).block().get("error");
        org.junit.jupiter.api.Assertions.assertTrue(msg.contains("Invalid meal type"));
    }

    @Test
    void handlesInvalidBookingStatusEnum() {
        var ex = new com.fasterxml.jackson.databind.exc.InvalidFormatException(
                null, "bad", "BAD", BookingStatus.class);
        HttpMessageNotReadableException http = new HttpMessageNotReadableException("bad", ex, null);
        String msg = handler.handleInvalidJson(http).block().get("error");
        org.junit.jupiter.api.Assertions.assertTrue(msg.contains("Invalid booking status"));
    }

    @Test
    void handlesDateParse() {
        java.time.format.DateTimeParseException dtEx =
                new java.time.format.DateTimeParseException("bad", "xx", 0);
        HttpMessageNotReadableException http = new HttpMessageNotReadableException("bad", dtEx, null);
        String msg = handler.handleInvalidJson(http).block().get("error");
        org.junit.jupiter.api.Assertions.assertTrue(msg.contains("Invalid date format"));
    }

    @Test
    void handlesValidationErrorsWithFieldMessages() throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "req");
        bindingResult.addError(new FieldError("req", "field", "must not be null"));
        MethodParameter parameter = new MethodParameter(
                GlobalErrorHandlerTest.class.getDeclaredMethod("dummy", String.class), 0);
        WebExchangeBindException ex = new WebExchangeBindException(parameter, bindingResult);

        Map<String, String> result = handler.handleValidationErrors(ex).block();
        assertEquals("must not be null", result.get("field"));
    }

    @Test
    void handlesInvalidJsonFallback() {
        HttpMessageNotReadableException http =
                new HttpMessageNotReadableException("bad", new RuntimeException("boom"), null);
        Map<String, String> result = handler.handleInvalidJson(http).block();
        assertEquals("Invalid JSON request", result.get("error"));
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
        // Intentionally empty: only used to obtain a MethodParameter instance for binding tests.
    }
}
