package com.bookingservice.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.format.DateTimeParseException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.Gender;
import com.bookingservice.model.Meal;
import com.bookingservice.model.TripType;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import reactor.test.StepVerifier;

class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    @Test
    void handleValidationErrors_returnsFieldMessages() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "req");
        result.addError(new FieldError("req", "field1", "must not be empty"));
        WebExchangeBindException ex =
                new WebExchangeBindException(Mockito.mock(org.springframework.core.MethodParameter.class), result);

        StepVerifier.create(handler.handleValidationErrors(ex))
                .assertNext(map -> assertEquals("must not be empty", map.get("field1")))
                .verifyComplete();
    }

    @Test
    void handleValidationException_wrapsMessage() {
        StepVerifier.create(handler.handleValidationException(new ValidationException("oops")))
                .assertNext(map -> assertEquals("oops", map.get("error")))
                .verifyComplete();
    }

    @Test
    void handleNotFound_wrapsMessage() {
        StepVerifier.create(handler.handleNotFound(new ResourceNotFoundException("missing")))
                .assertNext(map -> assertEquals("missing", map.get("error")))
                .verifyComplete();
    }

    @Test
    void handleInvalidJson_tripTypeError() {
        assertInvalidFormatMessage(TripType.class, "Invalid trip type. Allowed values: [ONE_WAY, ROUND_TRIP]");
    }

    @Test
    void handleInvalidJson_genderError() {
        assertInvalidFormatMessage(Gender.class, "Invalid gender. Allowed values: [MALE, FEMALE, OTHER]");
    }

    @Test
    void handleInvalidJson_mealError() {
        assertInvalidFormatMessage(Meal.class, "Invalid meal type. Allowed values: [VEG, NON_VEG]");
    }

    @Test
    void handleInvalidJson_bookingStatusError() {
        assertInvalidFormatMessage(BookingStatus.class, "Invalid booking status. Allowed values: [CONFIRMED, CANCELLED]");
    }

    @Test
    void handleInvalidJson_booleanError() {
        assertInvalidFormatMessage(Boolean.class, "Invalid boolean value. Allowed values: true or false");
    }

    @Test
    void handleInvalidJson_dateParseError() {
        DateTimeParseException cause = new DateTimeParseException("bad date", "2024-99-99", 0);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad json", cause, null);

        StepVerifier.create(handler.handleInvalidJson(ex))
                .assertNext(map -> assertEquals("Invalid date format. Use yyyy-MM-dd", map.get("error")))
                .verifyComplete();
    }

    @Test
    void handleInvalidJson_fallback() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("bad", new RuntimeException("boom"), null);

        StepVerifier.create(handler.handleInvalidJson(ex))
                .assertNext(map -> assertEquals("Invalid JSON request", map.get("error")))
                .verifyComplete();
    }

    @Test
    void handleOthers_wrapsGenericMessage() {
        StepVerifier.create(handler.handleOthers(new Exception("generic")))
                .assertNext(map -> assertEquals("generic", map.get("error")))
                .verifyComplete();
    }

    private void assertInvalidFormatMessage(Class<?> targetType, String expected) {
        InvalidFormatException cause = InvalidFormatException.from(
                null, "bad", "value", targetType);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad json", cause, null);

        StepVerifier.create(handler.handleInvalidJson(ex))
                .assertNext(map -> assertEquals(expected, map.get("error")))
                .verifyComplete();
    }
}