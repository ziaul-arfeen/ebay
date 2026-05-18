package assignment.ebay.dto;

import jakarta.validation.constraints.*;

public record CreateBookingRequest(
        @NotBlank(message = "Flight number is required")
        String flightNumber,

        @NotBlank(message = "Passenger name is required")
        String passengerName,

        @NotBlank(message = "Passenger email is required")
        @Email(message = "Passenger email must be valid")
        String passengerEmail,

        @Min(value = 1, message = "Must book at least 1 seat")
        @Max(value = 9, message = "Cannot book more than 9 seats in a single booking")
        int seats
) {}

