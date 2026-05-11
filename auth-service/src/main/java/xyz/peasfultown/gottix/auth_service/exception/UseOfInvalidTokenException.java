package xyz.peasfultown.gottix.auth_service.exception;

import org.springframework.http.HttpStatus;

public class UseOfInvalidTokenException extends CustomErrorResponseException {
    public UseOfInvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
