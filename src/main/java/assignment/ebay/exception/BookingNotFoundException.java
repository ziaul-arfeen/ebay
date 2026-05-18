package assignment.ebay.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String bookingReference) {
        super("Booking not found: " + bookingReference);
    }
}

