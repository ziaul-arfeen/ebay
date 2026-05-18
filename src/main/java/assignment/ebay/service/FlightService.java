package assignment.ebay.service;

import assignment.ebay.dto.CreateFlightRequest;
import assignment.ebay.exception.FlightAlreadyExistsException;
import assignment.ebay.exception.FlightNotFoundException;
import assignment.ebay.model.Flight;
import assignment.ebay.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class FlightService {

    private final FlightRepository flightRepository;

    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public Flight createFlight(CreateFlightRequest request) {
        if (flightRepository.existsByFlightNumber(request.flightNumber())) {
            throw new FlightAlreadyExistsException(request.flightNumber());
        }
        Flight flight = new Flight(
                request.flightNumber(),
                request.origin(),
                request.destination(),
                request.departureTime(),
                request.totalSeats(),
                request.price()
        );
        return flightRepository.save(flight);
    }

    public Flight getFlight(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new FlightNotFoundException(flightNumber));
    }

    public Collection<Flight> getAllFlights() {
        return flightRepository.findAll();
    }
}

