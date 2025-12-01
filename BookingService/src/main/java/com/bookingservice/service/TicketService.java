package com.bookingservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookingservice.exceptions.ResourceNotFoundException;
import com.bookingservice.exceptions.ValidationException;
import com.bookingservice.model.Booking;
import com.bookingservice.repository.BookingRepository;

import reactor.core.publisher.Mono;

@Service
public class TicketService {

    private final BookingRepository bookingRepository;

    @Autowired
    public TicketService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Mono<Booking> getTicketByPnr(String pnr) {

        if (pnr == null || pnr.trim().isEmpty()) {
            return Mono.error(new ValidationException("PNR cannot be empty"));
        }

        if (pnr.length() != 6) {
            return Mono.error(new ValidationException("PNR must be exactly 6 characters"));
        }

        if (!pnr.matches("^[A-Z0-9]+$")) {
            return Mono.error(new ValidationException("PNR must be alphanumeric"));
        }

        return bookingRepository.findByPnrOutbound(pnr)
                .switchIfEmpty(
                        bookingRepository.findByPnrReturn(pnr)
                                .switchIfEmpty(Mono.error(
                                        new ResourceNotFoundException("PNR not found")
                                ))
                );
    }
}
