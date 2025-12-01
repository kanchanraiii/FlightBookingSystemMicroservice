package com.flightservice.exceptions;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.flightservice.model.Cities;
import com.flightservice.model.TripType;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalErrorHandler {

    private static final String ERROR_MESSAGE = "error";
    private static final String DEFAULT_JSON_ERROR = "Invalid JSON request";
    private static final String DATE_ERROR = "Invalid date format. Use yyyy-MM-dd";
    private static final String BOOLEAN_ERROR = "Invalid boolean value. Allowed values: true or false";

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<Map<String, String>> handleValidationErrors(WebExchangeBindException exception) {
        Map<String, String> errorMap = new LinkedHashMap<>();
        exception.getFieldErrors().forEach(err -> errorMap.putIfAbsent(err.getField(), err.getDefaultMessage()));
        return Mono.just(errorMap);
    }

    @ExceptionHandler(ValidationException.class)
    public Mono<Map<String, String>> handleCustomValidation(ValidationException ex) {
        return Mono.just(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return Mono.just(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Mono<Map<String, String>> handleInvalidJson(HttpMessageNotReadableException ex) {
        String message = resolveMessage(ex.getCause());
        return Mono.just(Map.of(ERROR_MESSAGE, message));
    }

    @ExceptionHandler(Exception.class)
    public Mono<Map<String, String>> handleOthers(Exception ex) {
        return Mono.just(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    private static String resolveMessage(Throwable cause) {
        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidEx) {
            return resolveForTarget(invalidEx.getTargetType());
        }
        if (cause instanceof DateTimeParseException) {
            return DATE_ERROR;
        }
        return DEFAULT_JSON_ERROR;
    }

    private static String resolveForTarget(Class<?> type) {
        if (type == Cities.class) {
            return "Invalid city. Allowed values: " + Arrays.toString(Cities.values());
        }
        if (type == TripType.class) {
            return "Invalid trip type. Allowed values: " + Arrays.toString(TripType.values());
        }
        if (type == Boolean.class) {
            return BOOLEAN_ERROR;
        }
        return DEFAULT_JSON_ERROR;
    }
}
