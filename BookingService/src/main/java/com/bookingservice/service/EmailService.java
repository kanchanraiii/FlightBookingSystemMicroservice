package com.bookingservice.service;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.bookingservice.model.Booking;
import com.bookingservice.model.BookingEventType;

import reactor.core.publisher.Mono;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final JavaMailSender mailSender;
    private final boolean emailEnabled;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        @Value("${booking.email.enabled:false}") boolean emailEnabled,
                        @Value("${booking.email.from:}") String from) {
        this.mailSender = mailSender;
        this.emailEnabled = emailEnabled;
        this.from = from;
    }

    public Mono<Void> sendBookingNotification(Booking booking, BookingEventType type) {
        if (!emailEnabled || booking == null || !StringUtils.hasText(booking.getContactEmail())) {
            return Mono.empty();
        }

        return Mono.fromRunnable(() -> mailSender.send(buildMessage(booking, type)))
                .then()
                .doOnError(ex -> log.error("Failed to send booking email", ex))
                .onErrorResume(ex -> Mono.empty());
    }

    private SimpleMailMessage buildMessage(Booking booking, BookingEventType type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(booking.getContactEmail());
        if (StringUtils.hasText(from)) {
            message.setFrom(from);
        }
        message.setSubject(subject(booking, type));
        message.setText(body(booking, type));
        return message;
    }

    private String subject(Booking booking, BookingEventType type) {
        String pnr = booking.getPnrOutbound();
        return switch (type) {
            case BOOKED -> "Ticket booked - PNR " + pnr;
            case CANCELLED -> "Ticket cancelled - PNR " + pnr;
        };
    }

    private String body(Booking b, BookingEventType type) {
        String headline = type == BookingEventType.BOOKED ? "Your ticket is booked." : "Your ticket is cancelled.";
        String returnLine = b.getReturnFlight() == null ? "Return flight: N/A" : "Return flight: " + b.getReturnFlight();
        String returnPnr = StringUtils.hasText(b.getPnrReturn()) ? "Return PNR: " + b.getPnrReturn() : "Return PNR: N/A";

        return """
                %s
                PNR: %s
                %s
                Outbound flight: %s
                %s
                Passengers: %d
                Status: %s
                """.formatted(
                headline,
                b.getPnrOutbound(),
                returnPnr,
                b.getOutboundFlightId(),
                returnLine,
                b.getTotalPassengers(),
                b.getStatus());
    }
}
