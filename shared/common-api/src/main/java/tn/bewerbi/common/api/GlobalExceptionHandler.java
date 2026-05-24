package tn.bewerbi.common.api;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
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

    /**
     * Spring Security throws BadCredentialsException for wrong-password and
     * unknown-user (deliberately ambiguous to prevent enumeration). Without
     * this handler it falls through to {@link #generic(Exception)} below and
     * becomes a 500 INTERNAL_ERROR — so today, a user who mistypes their
     * password sees "Something went wrong". Mapped to 401 here so the client
     * can render a proper "invalid credentials" message.
     *
     * DisabledException and LockedException return the same shape but get
     * distinct error codes so the client can localize differently (e.g. show
     * an unlock-flow CTA when locked).
     *
     * Generic AuthenticationException catches anything else from the Spring
     * Security auth pipeline (e.g. AuthenticationServiceException from a
     * downstream lookup failing) and still avoids a 500 leak.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> badCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, "AUTH_INVALID",
                        "Invalid credentials", "error.auth.invalid"));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> disabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, "AUTH_DISABLED",
                        "Account disabled", "error.auth.disabled"));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiError> locked(LockedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, "AUTH_LOCKED",
                        "Account locked", "error.auth.locked"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> authentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.of(401, "AUTH_FAILED",
                        "Authentication failed", "error.auth.failed"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> accessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.of(403, "ACCESS_DENIED",
                        "Access denied", "error.access.denied"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError()
                .body(ApiError.of(500, "INTERNAL_ERROR",
                        "Something went wrong", "error.internal"));
    }
}
