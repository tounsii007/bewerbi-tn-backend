package tn.bewerbi.common.api;

import java.time.Instant;
import java.util.List;

/** Canonical error envelope returned by every service. */
public record ApiError(
        int status,
        String code,
        String message,
        /** Used as a key in the i18n-service for client-side translation. */
        String messageKey,
        List<FieldViolation> violations,
        Instant timestamp) {

    public static ApiError of(int status, String code, String message, String messageKey) {
        return new ApiError(status, code, message, messageKey, List.of(), Instant.now());
    }

    public static ApiError of(int status, String code, String message, String messageKey,
                              List<FieldViolation> violations) {
        return new ApiError(status, code, message, messageKey, violations, Instant.now());
    }

    public record FieldViolation(String field, String message, String messageKey) {}
}
