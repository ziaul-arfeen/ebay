package assignment.ebay.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private String createFlightJson(String flightNumber, int seats) {
        String departure = LocalDateTime.now().plusDays(10).format(FMT);
        return """
                {
                  "flightNumber": "%s",
                  "origin": "New York (JFK)",
                  "destination": "London (LHR)",
                  "departureTime": "%s",
                  "totalSeats": %d,
                  "price": 499.99
                }
                """.formatted(flightNumber, departure, seats);
    }

    @Test
    void shouldBookAndCancelSuccessfully() throws Exception {
        String flightNum = "TS100";

        // Create flight
        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createFlightJson(flightNum, 50)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value(flightNum))
                .andExpect(jsonPath("$.availableSeats").value(50));

        // Book 2 seats
        MvcResult result = mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "flightNumber": "%s",
                                  "passengerName": "Alice Smith",
                                  "passengerEmail": "alice@example.com",
                                  "seats": 2
                                }
                                """.formatted(flightNum)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingReference", startsWith("BK-")))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalPrice").value(999.98))
                .andReturn();

        // Verify seats decremented
        mockMvc.perform(get("/flights/" + flightNum))
                .andExpect(jsonPath("$.availableSeats").value(48));

        // Extract booking reference
        String body = result.getResponse().getContentAsString();
        String ref = body.replaceAll(".*\"bookingReference\":\"([^\"]+)\".*", "$1");

        // Cancel booking
        mockMvc.perform(delete("/bookings/" + ref))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify seats restored
        mockMvc.perform(get("/flights/" + flightNum))
                .andExpect(jsonPath("$.availableSeats").value(50));
    }

    @Test
    void shouldReturn409WhenOverbooking() throws Exception {
        String flightNum = "TS101";

        mockMvc.perform(post("/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createFlightJson(flightNum, 1)))
                .andExpect(status().isCreated());

        // Book the only seat
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "flightNumber": "%s", "passengerName": "A", "passengerEmail": "a@b.com", "seats": 1 }
                                """.formatted(flightNum)))
                .andExpect(status().isCreated());

        // Attempt to overbook
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "flightNumber": "%s", "passengerName": "B", "passengerEmail": "b@b.com", "seats": 1 }
                                """.formatted(flightNum)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404ForUnknownFlight() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "flightNumber": "XX999", "passengerName": "X", "passengerEmail": "x@x.com", "seats": 1 }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400ForInvalidBookingRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "flightNumber": "", "passengerName": "", "passengerEmail": "not-an-email", "seats": 0 }
                                """))
                .andExpect(status().isBadRequest());
    }
}

