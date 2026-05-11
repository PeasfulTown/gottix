package xyz.peasfultown.gottix.auth_service.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends CustomErrorResponseException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
