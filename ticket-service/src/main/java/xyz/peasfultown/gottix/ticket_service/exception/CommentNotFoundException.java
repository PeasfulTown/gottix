package xyz.peasfultown.gottix.ticket_service.exception;

import org.springframework.http.HttpStatus;

public class CommentNotFoundException extends BaseException {
    public CommentNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", message);
    }
}
