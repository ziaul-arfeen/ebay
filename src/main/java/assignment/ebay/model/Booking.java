package assignment.ebay.model;

import java.time.LocalDateTime;

public class Booking {

    private final String bookingReference;
    private final String flightNumber;
    private final String passengerName;
    private final String passengerEmail;
    private final int seats;
    private final double totalPrice;
    private final LocalDateTime bookedAt;
    private BookingStatus status;

    public Booking(String bookingReference, String flightNumber,
                   String passengerName, String passengerEmail,
                   int seats, double totalPrice) {
        this.bookingReference = bookingReference;
        this.flightNumber = flightNumber;
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.bookedAt = LocalDateTime.now();
        this.status = BookingStatus.CONFIRMED;
    }

    public String getBookingReference() { return bookingReference; }
    public String getFlightNumber() { return flightNumber; }
    public String getPassengerName() { return passengerName; }
    public String getPassengerEmail() { return passengerEmail; }
    public int getSeats() { return seats; }
    public double getTotalPrice() { return totalPrice; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public BookingStatus getStatus() { return status; }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public boolean isActive() {
        return this.status == BookingStatus.CONFIRMED;
    }
}

