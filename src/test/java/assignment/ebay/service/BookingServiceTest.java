package assignment.ebay.service;

import assignment.ebay.dto.CreateBookingRequest;
import assignment.ebay.dto.CreateFlightRequest;
import assignment.ebay.exception.BookingNotFoundException;
import assignment.ebay.exception.FlightAlreadyExistsException;
import assignment.ebay.exception.FlightNotFoundException;
import assignment.ebay.exception.OverbookingException;
import assignment.ebay.model.Booking;
import assignment.ebay.model.BookingStatus;
import assignment.ebay.model.Flight;
import assignment.ebay.repository.BookingRepository;
import assignment.ebay.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class BookingServiceTest {

    private FlightService flightService;
    private BookingService bookingService;

    private static final String FLIGHT_NUMBER = "AA101";
    private static final int TOTAL_SEATS = 10;

    @BeforeEach
    void setUp() {
        FlightRepository flightRepo = new FlightRepository();
        BookingRepository bookingRepo = new BookingRepository();
        flightService = new FlightService(flightRepo);
        bookingService = new BookingService(flightService, bookingRepo);

        flightService.createFlight(new CreateFlightRequest(
                FLIGHT_NUMBER, "JFK", "LAX",
                LocalDateTime.now().plusDays(1), TOTAL_SEATS, 299.99));
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        Booking booking = bookingService.createBooking(
                new CreateBookingRequest(FLIGHT_NUMBER, "Alice", "alice@example.com", 2));

        assertThat(booking.getBookingReference()).startsWith("BK-");
        assertThat(booking.getFlightNumber()).isEqualTo(FLIGHT_NUMBER);
        assertThat(booking.getSeats()).isEqualTo(2);
        assertThat(booking.getTotalPrice()).isEqualTo(299.99 * 2);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void shouldDecreaseAvailableSeatsAfterBooking() {
        bookingService.createBooking(
                new CreateBookingRequest(FLIGHT_NUMBER, "Alice", "alice@example.com", 3));

        Flight flight = flightService.getFlight(FLIGHT_NUMBER);
        assertThat(flight.getAvailableSeats()).isEqualTo(TOTAL_SEATS - 3);
    }

    @Test
    void shouldThrowOverbookingExceptionWhenNotEnoughSeats() {
        assertThatThrownBy(() ->
                bookingService.createBooking(
                        new CreateBookingRequest(FLIGHT_NUMBER, "Bob", "bob@example.com", TOTAL_SEATS + 1))
        ).isInstanceOf(OverbookingException.class);
    }

    @Test
    void shouldNotAllowOverbookingAcrossMultipleBookings() {
        // Book all seats in two requests
        bookingService.createBooking(
                new CreateBookingRequest(FLIGHT_NUMBER, "Alice", "alice@example.com", 9));

        assertThatThrownBy(() ->
                bookingService.createBooking(
                        new CreateBookingRequest(FLIGHT_NUMBER, "Bob", "bob@example.com", 2))
        ).isInstanceOf(OverbookingException.class);
    }

    @Test
    void shouldReturnSeatsOnCancellation() {
        Booking booking = bookingService.createBooking(
                new CreateBookingRequest(FLIGHT_NUMBER, "Alice", "alice@example.com", 4));

        bookingService.cancelBooking(booking.getBookingReference());

        Flight flight = flightService.getFlight(FLIGHT_NUMBER);
        assertThat(flight.getAvailableSeats()).isEqualTo(TOTAL_SEATS);
    }

    @Test
    void shouldThrowWhenCancellingAlreadyCancelledBooking() {
        Booking booking = bookingService.createBooking(
                new CreateBookingRequest(FLIGHT_NUMBER, "Alice", "alice@example.com", 1));

        bookingService.cancelBooking(booking.getBookingReference());

        assertThatThrownBy(() -> bookingService.cancelBooking(booking.getBookingReference()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void shouldThrowWhenBookingNonExistentFlight() {
        assertThatThrownBy(() ->
                bookingService.createBooking(
                        new CreateBookingRequest("ZZ999", "Alice", "alice@example.com", 1))
        ).isInstanceOf(FlightNotFoundException.class);
    }

    @Test
    void shouldThrowWhenCancellingNonExistentBooking() {
        assertThatThrownBy(() -> bookingService.cancelBooking("BK-INVALID"))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void shouldThrowWhenCreatingDuplicateFlight() {
        assertThatThrownBy(() ->
                flightService.createFlight(new CreateFlightRequest(
                        FLIGHT_NUMBER, "JFK", "LAX",
                        LocalDateTime.now().plusDays(2), 100, 199.0))
        ).isInstanceOf(FlightAlreadyExistsException.class);
    }
}

