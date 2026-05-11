package xyz.peasfultown.gottix.auth_service.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends CustomErrorResponseException {
    public UserAlreadyExistsException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
