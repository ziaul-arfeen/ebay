package assignment.ebay.exception;

public class OverbookingException extends RuntimeException {
    public OverbookingException(String flightNumber, int requested, int available) {
        super("Cannot book %d seat(s) on flight %s: only %d seat(s) available"
                .formatted(requested, flightNumber, available));
    }
}

