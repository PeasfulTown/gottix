package xyz.peasfultown.gottix.auth_service.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BaseException {
    public UserAlreadyExistsException(String message) {
        super(message, "USER_ALREADY_EXISTS", HttpStatus.BAD_REQUEST);
    }
}
