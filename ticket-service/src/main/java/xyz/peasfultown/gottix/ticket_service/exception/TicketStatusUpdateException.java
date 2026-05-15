package xyz.peasfultown.gottix.ticket_service.exception;

import org.springframework.http.HttpStatus;

public class TicketStatusUpdateException extends BaseException {
    public TicketStatusUpdateException(String message) {
        super(HttpStatus.BAD_REQUEST, "ILLEGAL_TICKET_STATUS_UPDATE", message);
    }
}
