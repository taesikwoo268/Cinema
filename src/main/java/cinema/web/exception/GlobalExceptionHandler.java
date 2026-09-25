package cinema.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cinema.application.common.exceptions.ConflictException;
import cinema.application.common.exceptions.FeatureNotImplementedException;
import cinema.application.common.exceptions.ForbiddenException;
import cinema.application.common.exceptions.NotFoundException;
import cinema.application.common.exceptions.ValidationException;
import cinema.web.responses.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> malformedRequest(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse("VALIDATION", "Request body must be valid JSON with the expected field types"));
    }

    @ExceptionHandler(FeatureNotImplementedException.class)
    public ResponseEntity<ErrorResponse> notImplemented(FeatureNotImplementedException ex) {
        return error(HttpStatus.NOT_IMPLEMENTED, "NOT_IMPLEMENTED", ex);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> conflict(ConflictException ex) {
        return error(HttpStatus.CONFLICT, "CONFLICT", ex);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> forbidden(ForbiddenException ex) {
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN", ex);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> invalid(ValidationException ex) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION", ex);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, RuntimeException ex) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, ex.getMessage()));
    }
}
