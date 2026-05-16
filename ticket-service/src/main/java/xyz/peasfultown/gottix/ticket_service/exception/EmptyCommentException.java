package xyz.peasfultown.gottix.ticket_service.exception;

import org.springframework.http.HttpStatus;

public class EmptyCommentException extends BaseException {
    public EmptyCommentException(String message) {
        super(HttpStatus.BAD_REQUEST, "EMPTY_COMMENT", message);
    }
}
