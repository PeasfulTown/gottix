package xyz.peasfultown.gottix.auth_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BaseException {
    public InvalidTokenException(String message) {
        super(message, "INVALID_TOKEN", HttpStatus.UNAUTHORIZED);
    }
}
