package xyz.peasfultown.gottix.auth_service.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends BaseException {
    public AccessDeniedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.FORBIDDEN);
    }
}
