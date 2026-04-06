package cvut.fel.sit.mojefinance.user.api.exception;

import cvut.fel.sit.mojefinance.openapi.model.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class UserExceptionHandler {

    private static void logException(Exception e) {
        log.warn("Exception caught: {}", e.getMessage());
    }

    private static ErrorResponse errorInfo(Throwable e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(e.getMessage());
        return errorResponse;
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ErrorResponse> persistenceException(PersistenceException e) {
        logException(e);
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        return new ResponseEntity<>(errorInfo(cause), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> nullPointerException(NullPointerException e) {
        logException(e);
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        return new ResponseEntity<>(errorInfo(cause), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFoundException(EntityNotFoundException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(e), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> validationException(ValidationException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(e), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedException(AccessDeniedException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(e), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> methodArgNotValidException(MethodArgumentNotValidException e) {
        logException(e);
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult()
                .getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegalArgumentException(IllegalArgumentException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(e), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> unsupportedMediaTypeException(HttpMediaTypeNotSupportedException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(e), HttpStatus.BAD_REQUEST);
    }
}
