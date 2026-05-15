package xyz.peasfultown.gottix.ticket_service.exception;

import org.springframework.http.HttpStatus;

public class TicketNotFoundException extends BaseException {
    public TicketNotFoundException(String ticketId) {
        super(HttpStatus.NOT_FOUND, "TICKET_NOT_FOUND", String.format("ticket not found by ID: %s", ticketId));
    }

    public TicketNotFoundException(String ticketId, String customerId) {
        super(HttpStatus.NOT_FOUND, "TICKET_NOT_FOUND", String.format("ticket not found by ID: %s for customer ID: %s", ticketId, customerId));
    }
}
