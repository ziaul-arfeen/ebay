package assignment.ebay.dto;

import assignment.ebay.model.Booking;
import assignment.ebay.model.BookingStatus;
import java.time.LocalDateTime;

public record BookingResponse(
        String bookingReference,
        String flightNumber,
        String passengerName,
        String passengerEmail,
        int seats,
        double totalPrice,
        LocalDateTime bookedAt,
        BookingStatus status
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getBookingReference(),
                booking.getFlightNumber(),
                booking.getPassengerName(),
                booking.getPassengerEmail(),
                booking.getSeats(),
                booking.getTotalPrice(),
                booking.getBookedAt(),
                booking.getStatus()
        );
    }
}

