package xyz.peasfultown.gottix.auth_service.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CustomErrorResponseException {
    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
