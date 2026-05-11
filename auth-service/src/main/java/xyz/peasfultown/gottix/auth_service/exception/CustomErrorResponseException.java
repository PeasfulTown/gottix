package xyz.peasfultown.gottix.auth_service.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponseException;

public class CustomErrorResponseException extends ErrorResponseException {
    public CustomErrorResponseException(HttpStatusCode status, String message) {
        super(status);
        super.setType(null);
        super.setDetail(message);
    }
}
