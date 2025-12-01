package com.bookingservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.bookingservice.exceptions.ResourceNotFoundException;
import com.bookingservice.exceptions.ValidationException;

class ExceptionClassesTest {

    @Test
    void validationExceptionMessage() {
        assertEquals("oops", new ValidationException("oops").getMessage());
    }

    @Test
    void resourceNotFoundMessage() {
        assertEquals("missing", new ResourceNotFoundException("missing").getMessage());
    }
}
