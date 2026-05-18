package assignment.ebay.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class Flight {

    private final String flightNumber;
    private final String origin;
    private final String destination;
    private final LocalDateTime departureTime;
    private final int totalSeats;
    private final AtomicInteger availableSeats;
    private final double price;

    public Flight(String flightNumber, String origin, String destination,
                  LocalDateTime departureTime, int totalSeats, double price) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.availableSeats = new AtomicInteger(totalSeats);
        this.price = price;
    }

    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() { return availableSeats.get(); }
    public double getPrice() { return price; }

    /**
     * Atomically reserves {@code seats} if enough seats are available.
     * Returns true if reservation succeeded, false if it would cause overbooking.
     */
    public boolean reserveSeats(int seats) {
        while (true) {
            int current = availableSeats.get();
            if (current < seats) return false;
            if (availableSeats.compareAndSet(current, current - seats)) return true;
        }
    }

    /**
     * Returns previously-reserved seats back to the pool.
     */
    public void releaseSeats(int seats) {
        availableSeats.addAndGet(seats);
    }
}

