package assignment.ebay;

import assignment.ebay.dto.CreateFlightRequest;
import assignment.ebay.service.FlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds a few sample flights on startup so the API can be exercised immediately.
 * All data is in-memory and will be lost on restart.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final FlightService flightService;

    public DataInitializer(FlightService flightService) {
        this.flightService = flightService;
    }

    @Override
    public void run(ApplicationArguments args) {
        createFlight("AA101", "New York (JFK)", "Los Angeles (LAX)",
                LocalDateTime.now().plusDays(7), 180, 299.99);
        createFlight("BA202", "London (LHR)", "Paris (CDG)",
                LocalDateTime.now().plusDays(3), 120, 149.50);
        createFlight("LH303", "Frankfurt (FRA)", "Tokyo (NRT)",
                LocalDateTime.now().plusDays(14), 250, 899.00);
        createFlight("EK404", "Dubai (DXB)", "Sydney (SYD)",
                LocalDateTime.now().plusDays(5), 300, 749.00);

        log.info("Sample flights loaded. Registered {} flights.", 4);
    }

    private void createFlight(String flightNumber, String origin, String destination,
                               LocalDateTime departure, int seats, double price) {
        flightService.createFlight(new CreateFlightRequest(
                flightNumber, origin, destination, departure, seats, price));
        log.info("  -> {} | {} → {} | {} seats @ ${}", flightNumber, origin, destination, seats, price);
    }
}

