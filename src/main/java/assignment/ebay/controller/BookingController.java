package assignment.ebay.controller;

import assignment.ebay.dto.BookingResponse;
import assignment.ebay.dto.CreateBookingRequest;
import assignment.ebay.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Book seats on a flight.
     * POST /bookings
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = BookingResponse.from(bookingService.createBooking(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cancel an existing booking. Seats are returned to the flight inventory.
     * DELETE /bookings/{bookingReference}
     */
    @DeleteMapping("/{bookingReference}")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable String bookingReference) {
        BookingResponse response = BookingResponse.from(bookingService.cancelBooking(bookingReference));
        return ResponseEntity.ok(response);
    }
}

