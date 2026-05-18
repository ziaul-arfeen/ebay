package assignment.ebay.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CreateFlightRequest(
        @NotBlank(message = "Flight number is required")
        @Pattern(regexp = "^[A-Z]{2}\\d{3,4}$", message = "Flight number must match format e.g. AA123")
        String flightNumber,

        @NotBlank(message = "Origin is required")
        String origin,

        @NotBlank(message = "Destination is required")
        String destination,

        @NotNull(message = "Departure time is required")
        @Future(message = "Departure time must be in the future")
        LocalDateTime departureTime,

        @Min(value = 1, message = "Total seats must be at least 1")
        @Max(value = 853, message = "Total seats cannot exceed 853")
        int totalSeats,

        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        double price
) {}

