# Flight Ticket Booking API

A REST API for a flight ticket booking system, built with **Spring Boot 3.4** and **Java 21**.  
All data is stored in-memory (no database required). Sample flights are seeded on startup.

---

## How to Run

### Prerequisites

- **Java 21+** installed ([download](https://adoptium.net/))
- No database, Docker, or external services required

### Start the application

```bash
./gradlew bootRun
```

The service starts at **http://localhost:8085**.

### Run tests

```bash
./gradlew clean test
```

14 tests total: 9 unit tests + 4 integration tests + 1 context-loads test.

---

## Seeded Sample Data

On startup, 4 flights are pre-loaded so you can immediately test booking:

| Flight  | Route                                    | Seats | Price    |
|---------|------------------------------------------|-------|----------|
| AA101   | New York (JFK) → Los Angeles (LAX)      | 180   | $299.99  |
| BA202   | London (LHR) → Paris (CDG)              | 120   | $149.50  |
| LH303   | Frankfurt (FRA) → Tokyo (NRT)           | 250   | $899.00  |
| EK404   | Dubai (DXB) → Sydney (SYD)              | 300   | $749.00  |

---

## API Endpoints

### Flights

| Method | Path                        | Description                | Status Codes     |
|--------|-----------------------------|----------------------------|------------------|
| `POST` | `/flights`                 | Register a new flight      | 201, 400, 409    |
| `GET`  | `/flights`                 | List all flights           | 200              |
| `GET`  | `/flights/{flightNumber}`  | Get a specific flight      | 200, 404         |

### Bookings

| Method   | Path                              | Description                              | Status Codes     |
|----------|-----------------------------------|------------------------------------------|------------------|
| `POST`   | `/bookings`                      | Book seats on a flight                   | 201, 400, 404, 409 |
| `DELETE`  | `/bookings/{bookingReference}`  | Cancel a booking (seats are returned)    | 200, 404, 409    |

---

## Example Requests

### 1. Create a new flight

```bash
curl -X POST http://localhost:8085/flights \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "UA500",
    "origin": "Chicago (ORD)",
    "destination": "Miami (MIA)",
    "departureTime": "2026-12-01T08:00:00",
    "totalSeats": 150,
    "price": 189.99
  }'
```

**Response** `201 Created`:
```json
{
  "flightNumber": "UA500",
  "origin": "Chicago (ORD)",
  "destination": "Miami (MIA)",
  "departureTime": "2026-12-01T08:00:00",
  "totalSeats": 150,
  "availableSeats": 150,
  "price": 189.99
}
```

### 2. List all flights

```bash
curl http://localhost:8085/flights
```

### 3. Get a specific flight

```bash
curl http://localhost:8085/flights/AA101
```

### 4. Book seats on a flight

```bash
curl -X POST http://localhost:8085/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "AA101",
    "passengerName": "Alice Smith",
    "passengerEmail": "alice@example.com",
    "seats": 2
  }'
```

**Response** `201 Created`:
```json
{
  "bookingReference": "BK-A1B2C3D4",
  "flightNumber": "AA101",
  "passengerName": "Alice Smith",
  "passengerEmail": "alice@example.com",
  "seats": 2,
  "totalPrice": 599.98,
  "bookedAt": "2026-05-18T10:00:00",
  "status": "CONFIRMED"
}
```

### 5. Cancel a booking

```bash
curl -X DELETE http://localhost:8085/bookings/BK-A1B2C3D4
```

**Response** `200 OK`:
```json
{
  "bookingReference": "BK-A1B2C3D4",
  "flightNumber": "AA101",
  "passengerName": "Alice Smith",
  "passengerEmail": "alice@example.com",
  "seats": 2,
  "totalPrice": 599.98,
  "bookedAt": "2026-05-18T10:00:00",
  "status": "CANCELLED"
}
```

### 6. Overbooking attempt (409 Conflict)

```bash
# After all seats are booked:
curl -X POST http://localhost:8085/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "flightNumber": "AA101",
    "passengerName": "Bob",
    "passengerEmail": "bob@example.com",
    "seats": 1
  }'
```

**Response** `409 Conflict`:
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Cannot book 1 seat(s) on flight AA101: only 0 seat(s) available",
  "instance": "/bookings"
}
```

---

## Error Handling

All errors return [RFC 9457 Problem Detail](https://www.rfc-editor.org/rfc/rfc9457) JSON responses:

| HTTP Status | When                                                     |
|-------------|----------------------------------------------------------|
| `400`       | Invalid request body (missing fields, bad email, etc.)   |
| `404`       | Flight or booking not found                              |
| `409`       | Overbooking, duplicate flight, or already-cancelled booking |

---

## Design Decisions

- **In-memory storage** — `ConcurrentHashMap` per entity type; no persistence across restarts.
- **Thread-safe overbooking prevention** — `AtomicInteger` + CAS (compare-and-swap) loop on `Flight.availableSeats` ensures concurrent bookings never exceed capacity without requiring synchronized blocks.
- **Cancellation restores seats** — `DELETE /bookings/{ref}` returns the reserved seats to the flight pool, making them available for new bookings.
- **Bean Validation** — `@Valid` on all request bodies enforces required fields, valid email format, seat count limits, and future departure times before business logic runs.
- **RFC 9457 Problem Details** — All errors use Spring's `ProblemDetail` for structured, machine-readable error responses.

---

## What I Would Improve With More Time

1. **Persistence** — Replace `ConcurrentHashMap` with JPA + PostgreSQL so data survives restarts.
2. **Retrieve bookings** — Add `GET /bookings/{ref}` and `GET /bookings?email=...` endpoints.
3. **Idempotency keys** — Accept a client-supplied `Idempotency-Key` header on `POST /bookings` to safely retry without double-booking.
4. **Flight status lifecycle** — States like `SCHEDULED → BOARDING → DEPARTED → CANCELLED`; block bookings on departed/cancelled flights.
5. **Pagination & sorting** — `GET /flights` should support `?page=&size=&sort=departureTime`.
6. **OpenAPI / Swagger UI** — Add `springdoc-openapi` for self-documenting API.
7. **Structured logging & tracing** — Correlate logs with `bookingReference` and `flightNumber` via MDC.
8. **Concurrency stress tests** — Simulate concurrent bookings with virtual threads to verify no seat is double-booked under load.
9. **Input normalisation** — Normalise `flightNumber` to uppercase so `aa101` and `AA101` resolve to the same flight.
10. **Docker image** — Add a `Dockerfile` for containerised deployment.
