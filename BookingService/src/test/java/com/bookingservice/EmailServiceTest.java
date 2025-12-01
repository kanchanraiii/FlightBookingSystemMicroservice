package com.bookingservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.bookingservice.model.Booking;
import com.bookingservice.model.BookingEventType;
import com.bookingservice.model.BookingStatus;
import com.bookingservice.model.TripType;
import com.bookingservice.service.EmailService;

class EmailServiceTest {

    private final JavaMailSender sender = org.mockito.Mockito.mock(JavaMailSender.class);

    private EmailService service(boolean enabled, String from) {
        return new EmailService(sender, enabled, from);
    }

    @Test
    void sendsWhenEnabledAndEmailPresent() {
        EmailService svc = service(true, "from@test.com");
        svc.sendBookingNotification(sampleBooking(), BookingEventType.BOOKED).block();
        verify(sender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void skipsWhenDisabled() {
        EmailService svc = service(false, "from@test.com");
        svc.sendBookingNotification(sampleBooking(), BookingEventType.CANCELLED).block();
        verify(sender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void skipsWhenNoContactEmail() {
        EmailService svc = service(true, "from@test.com");
        Booking b = sampleBooking();
        b.setContactEmail(null);
        svc.sendBookingNotification(b, BookingEventType.BOOKED).block();
        verify(sender, never()).send(any(SimpleMailMessage.class));
    }

    private Booking sampleBooking() {
        Booking b = new Booking();
        b.setBookingId("B1");
        b.setPnrOutbound("ABC123");
        b.setOutboundFlightId("F1");
        b.setContactName("Jane Doe");
        b.setContactEmail("jane@example.com");
        b.setTotalPassengers(2);
        b.setStatus(BookingStatus.CONFIRMED);
        b.setTripType(TripType.ONE_WAY);
        return b;
    }
}
