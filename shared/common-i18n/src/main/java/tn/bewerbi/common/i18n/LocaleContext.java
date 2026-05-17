package tn.bewerbi.common.i18n;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads the requested locale from (in priority order):
 *   1. `X-Locale` header,
 *   2. `Accept-Language` header,
 * and binds it to a ThreadLocal for the duration of the request.
 *
 * The resolved locale is also written back to the response as
 * {@code Content-Language} so the client can tell which actual locale was served.
 */
@Component
public class LocaleContext extends OncePerRequestFilter {

    public static final Set<String> SUPPORTED = Set.of("de", "fr", "ar");
    public static final String DEFAULT = "de";

    private static final ThreadLocal<Locale> CURRENT = ThreadLocal.withInitial(() -> Locale.forLanguageTag(DEFAULT));

    public static Locale current() { return CURRENT.get(); }
    public static String currentTag() { return CURRENT.get().toLanguageTag(); }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("X-Locale");
        if (header == null || header.isBlank()) header = req.getHeader("Accept-Language");

        String tag = resolve(header);
        Locale locale = Locale.forLanguageTag(tag);
        CURRENT.set(locale);
        res.setHeader("Content-Language", tag);
        try {
            chain.doFilter(req, res);
        } finally {
            CURRENT.remove();
        }
    }

    private static String resolve(String header) {
        if (header == null) return DEFAULT;
        String[] parts = header.split("[,;]");
        for (String p : parts) {
            String tag = p.trim().toLowerCase();
            if (tag.contains("-")) tag = tag.substring(0, tag.indexOf('-'));
            if (SUPPORTED.contains(tag)) return tag;
        }
        return DEFAULT;
    }
}
