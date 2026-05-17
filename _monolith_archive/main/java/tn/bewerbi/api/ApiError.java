package tn.bewerbi.api;

import java.time.Instant;
import java.util.List;

public record ApiError(
        int status,
        String code,
        String message,
        List<FieldViolation> violations,
        Instant timestamp) {

    public static ApiError of(int status, String code, String message) {
        return new ApiError(status, code, message, List.of(), Instant.now());
    }

    public static ApiError of(int status, String code, String message, List<FieldViolation> violations) {
        return new ApiError(status, code, message, violations, Instant.now());
    }

    public record FieldViolation(String field, String message) {}
}
