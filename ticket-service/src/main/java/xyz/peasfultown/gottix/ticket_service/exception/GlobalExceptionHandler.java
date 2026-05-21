package xyz.peasfultown.gottix.ticket_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleRuntimeException(BaseException exception, WebRequest request) {
        log.error("request failed, code: {}, message: {}", exception.getErrorCode(), exception.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                exception.getHttpStatus(),
                exception.getMessage()
        );
        problem.setTitle(exception.getErrorCode());
        return status(problem.getStatus()).body(problem);
    }
}
