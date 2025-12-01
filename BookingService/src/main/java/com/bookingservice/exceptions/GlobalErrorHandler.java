package com.bookingservice.exceptions;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.Gender;
import com.bookingservice.model.Meal;
import com.bookingservice.model.TripType;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalErrorHandler {

    private static final String ERROR_MESSAGE = "error";

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<Map<String, String>> handleValidationErrors(WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );
        return Mono.just(errors);
    }

    // Custom validation exception
    @ExceptionHandler(ValidationException.class)
    public Mono<Map<String, String>> handleValidationException(ValidationException ex) {
        return Mono.just(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    // Resource not found
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return Mono.just(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Mono<Map<String, String>> handleInvalidJson(HttpMessageNotReadableException ex) {

        Map<String, String> error = new HashMap<>();
        Throwable cause = ex.getCause();

        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidEx) {
            Class<?> targetType = invalidEx.getTargetType();

            if (targetType == TripType.class) {
                error.put(ERROR_MESSAGE,
                        "Invalid trip type. Allowed values: " + Arrays.toString(TripType.values()));
                return Mono.just(error);
            }

            if (targetType == Gender.class) {
                error.put(ERROR_MESSAGE,
                        "Invalid gender. Allowed values: " + Arrays.toString(Gender.values()));
                return Mono.just(error);
            }

            if (targetType == Meal.class) {
                error.put(ERROR_MESSAGE,
                        "Invalid meal type. Allowed values: " + Arrays.toString(Meal.values()));
                return Mono.just(error);
            }

            if (targetType == BookingStatus.class) {
                error.put(ERROR_MESSAGE,
                        "Invalid booking status. Allowed values: "
                                + Arrays.toString(BookingStatus.values()));
                return Mono.just(error);
            }

            if (targetType == Boolean.class) {
                error.put(ERROR_MESSAGE,
                        "Invalid boolean value. Allowed values: true or false");
                return Mono.just(error);
            }
        }

        if (cause instanceof DateTimeParseException) {
            error.put(ERROR_MESSAGE, "Invalid date format. Use yyyy-MM-dd");
            return Mono.just(error);
        }

        error.put(ERROR_MESSAGE, "Invalid JSON request");
        return Mono.just(error);
    }

    // Fallback for unexpected errors
    @ExceptionHandler(Exception.class)
    public Mono<Map<String, String>> handleOthers(Exception ex) {
        return Mono.just(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }
}
