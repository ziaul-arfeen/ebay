package assignment.ebay.repository;

import assignment.ebay.model.Booking;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BookingRepository {

    private final ConcurrentHashMap<String, Booking> store = new ConcurrentHashMap<>();

    public Booking save(Booking booking) {
        store.put(booking.getBookingReference(), booking);
        return booking;
    }

    public Optional<Booking> findByReference(String bookingReference) {
        return Optional.ofNullable(store.get(bookingReference));
    }
}

