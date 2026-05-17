package tn.bewerbi.common.api;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import tn.bewerbi.common.api.exception.BadRequestException;

/** Helper to read the authenticated user id from the security context. */
public final class CurrentUser {
    private CurrentUser() {}

    public static UUID id() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new BadRequestException("No authenticated user", "error.auth.missing");
        }
        return UUID.fromString(auth.getName());
    }

    public static UUID idOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        try { return UUID.fromString(auth.getName()); } catch (Exception ignored) { return null; }
    }
}
