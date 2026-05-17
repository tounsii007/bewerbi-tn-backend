package tn.bewerbi.common.api;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.bewerbi.common.api.exception.DomainException;

/** Auto-imported by every service via {@code @Import(GlobalExceptionHandler.class)}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> domain(DomainException ex) {
        return ResponseEntity.status(ex.httpStatus())
                .body(ApiError.of(ex.httpStatus(), ex.code(), ex.getMessage(), ex.messageKey()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        List<ApiError.FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldViolation(
                        fe.getField(),
                        fe.getDefaultMessage(),
                        "error.validation." + fe.getField()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(400, "VALIDATION_FAILED",
                        "Request validation failed", "error.validation.failed", violations));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraint(ConstraintViolationException ex) {
        List<ApiError.FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldViolation(
                        v.getPropertyPath().toString(),
                        v.getMessage(),
                        "error.validation." + v.getPropertyPath()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(400, "VALIDATION_FAILED",
                        "Constraint violations", "error.validation.failed", violations));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError()
                .body(ApiError.of(500, "INTERNAL_ERROR",
                        "Something went wrong", "error.internal"));
    }
}
