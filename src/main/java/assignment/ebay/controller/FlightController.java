package assignment.ebay.controller;

import assignment.ebay.dto.CreateFlightRequest;
import assignment.ebay.dto.FlightResponse;
import assignment.ebay.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    /**
     * Register a new flight.
     * POST /flights
     */
    @PostMapping
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody CreateFlightRequest request) {
        FlightResponse response = FlightResponse.from(flightService.createFlight(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List all registered flights.
     * GET /flights
     */
    @GetMapping
    public List<FlightResponse> listFlights() {
        return flightService.getAllFlights().stream()
                .map(FlightResponse::from)
                .toList();
    }

    /**
     * Get a specific flight by flight number.
     * GET /flights/{flightNumber}
     */
    @GetMapping("/{flightNumber}")
    public FlightResponse getFlight(@PathVariable String flightNumber) {
        return FlightResponse.from(flightService.getFlight(flightNumber));
    }
}

