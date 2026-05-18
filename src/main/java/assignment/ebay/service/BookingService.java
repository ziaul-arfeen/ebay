package assignment.ebay.service;

import assignment.ebay.dto.CreateBookingRequest;
import assignment.ebay.exception.BookingNotFoundException;
import assignment.ebay.exception.OverbookingException;
import assignment.ebay.model.Booking;
import assignment.ebay.model.Flight;
import assignment.ebay.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookingService {

    private final FlightService flightService;
    private final BookingRepository bookingRepository;

    public BookingService(FlightService flightService, BookingRepository bookingRepository) {
        this.flightService = flightService;
        this.bookingRepository = bookingRepository;
    }

    public Booking createBooking(CreateBookingRequest request) {
        Flight flight = flightService.getFlight(request.flightNumber());

        boolean reserved = flight.reserveSeats(request.seats());
        if (!reserved) {
            throw new OverbookingException(request.flightNumber(), request.seats(), flight.getAvailableSeats());
        }

        double totalPrice = flight.getPrice() * request.seats();
        String reference = generateReference();

        Booking booking = new Booking(
                reference,
                request.flightNumber(),
                request.passengerName(),
                request.passengerEmail(),
                request.seats(),
                totalPrice
        );

        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(String bookingReference) {
        Booking booking = bookingRepository.findByReference(bookingReference)
                .orElseThrow(() -> new BookingNotFoundException(bookingReference));

        if (!booking.isActive()) {
            throw new IllegalStateException("Booking " + bookingReference + " is already cancelled");
        }

        booking.cancel();

        // Return seats to the flight inventory
        Flight flight = flightService.getFlight(booking.getFlightNumber());
        flight.releaseSeats(booking.getSeats());

        return booking;
    }

    private String generateReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

