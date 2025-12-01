package com.bookingservice.model;

import java.time.Instant;

public class BookingEvent {

    private BookingEventType eventType;
    private String bookingId;
    private String pnrOutbound;
    private String pnrReturn;
    private String outboundFlightId;
    private String returnFlightId;
    private String contactName;
    private String contactEmail;
    private int totalPassengers;
    private BookingStatus status;
    private TripType tripType;
    private Instant occurredAt;

    public BookingEventType getEventType() {
        return eventType;
    }

    public void setEventType(BookingEventType eventType) {
        this.eventType = eventType;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPnrOutbound() {
        return pnrOutbound;
    }

    public void setPnrOutbound(String pnrOutbound) {
        this.pnrOutbound = pnrOutbound;
    }

    public String getPnrReturn() {
        return pnrReturn;
    }

    public void setPnrReturn(String pnrReturn) {
        this.pnrReturn = pnrReturn;
    }

    public String getOutboundFlightId() {
        return outboundFlightId;
    }

    public void setOutboundFlightId(String outboundFlightId) {
        this.outboundFlightId = outboundFlightId;
    }

    public String getReturnFlightId() {
        return returnFlightId;
    }

    public void setReturnFlightId(String returnFlightId) {
        this.returnFlightId = returnFlightId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public int getTotalPassengers() {
        return totalPassengers;
    }

    public void setTotalPassengers(int totalPassengers) {
        this.totalPassengers = totalPassengers;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
