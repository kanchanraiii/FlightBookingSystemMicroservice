package com.bookingservice.exceptions;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    private static final String DEFAULT_JSON_ERROR = "Invalid JSON request";
    private static final String DATE_ERROR = "Invalid date format. Use yyyy-MM-dd";
    private static final String BOOLEAN_ERROR = "Invalid boolean value. Allowed values: true or false";
    private static final Map<Class<?>, String> ENUM_ERROR_MESSAGES = Map.of(
            TripType.class, enumMessage("trip type", TripType.values()),
            Gender.class, enumMessage("gender", Gender.values()),
            Meal.class, enumMessage("meal type", Meal.values()),
            BookingStatus.class, enumMessage("booking status", BookingStatus.values())
    );

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<Map<String, String>> handleValidationErrors(WebExchangeBindException ex) {
        Map<String, String> errors = ex.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage(),
                        (first, ignored) -> first,
                        LinkedHashMap::new));
        return Mono.just(errors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public Mono<Map<String, String>> handleValidationException(ValidationException ex) {
        return Mono.just(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return Mono.just(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(ServiceUnavailableException.class)
    public Mono<Map<String, String>> handleServiceUnavailable(ServiceUnavailableException ex) {
        return Mono.just(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Mono<Map<String, String>> handleInvalidJson(HttpMessageNotReadableException ex) {
        String message = messageFromCause(ex.getCause());
        return Mono.just(Map.of(ERROR_MESSAGE, message));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Mono<Map<String, String>> handleOthers(Exception ex) {
        return Mono.just(Map.of(ERROR_MESSAGE, ex.getMessage()));
    }

    private String messageFromCause(Throwable cause) {
        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidEx) {
            return Optional.ofNullable(ENUM_ERROR_MESSAGES.get(invalidEx.getTargetType()))
                    .orElseGet(() -> invalidEx.getTargetType() == Boolean.class
                            ? BOOLEAN_ERROR
                            : DEFAULT_JSON_ERROR);
        }

        if (cause instanceof DateTimeParseException) {
            return DATE_ERROR;
        }

        return DEFAULT_JSON_ERROR;
    }

    private static String enumMessage(String label, Enum<?>[] values) {
        return "Invalid " + label + ". Allowed values: " + Arrays.toString(values);
    }
}
