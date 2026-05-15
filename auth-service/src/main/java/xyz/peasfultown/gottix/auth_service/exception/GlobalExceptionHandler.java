package xyz.peasfultown.gottix.auth_service.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleRuntimeException(BaseException exception, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                exception.getHttpStatus(),
                exception.getMessage()
        );
        problem.setTitle(exception.getErrorCode());
        return status(problem.getStatus()).body(problem);
}

}
