package tn.bewerbi.i18n.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.i18n.domain.ReferenceEntry;
import tn.bewerbi.i18n.domain.ReferenceEntryRepository;
import tn.bewerbi.i18n.domain.ReferenceEntryTranslation;

@RestController
@RequestMapping("/api/v1/reference-data")
@Tag(name = "i18n: Reference Data")
public class ReferenceDataController {

    private final ReferenceService service;
    public ReferenceDataController(ReferenceService service) { this.service = service; }

    @GetMapping("/{type}")
    public List<ReferenceResponse> byType(
            @PathVariable String type,
            @RequestParam(defaultValue = "de") String locale) {
        return service.list(type, locale);
    }

    public record ReferenceResponse(String code, int sortOrder, String label, String hint,
                                    JsonNode metadata) {}

    @Service
    public static class ReferenceService {
        private final ReferenceEntryRepository repo;
        public ReferenceService(ReferenceEntryRepository repo) { this.repo = repo; }

        @Cacheable(value = "reference", key = "#type + ':' + #locale")
        public List<ReferenceResponse> list(String type, String locale) {
            return repo.listByType(type).stream()
                    .map(entry -> toResponse(entry, locale))
                    .toList();
        }

        private ReferenceResponse toResponse(ReferenceEntry entry, String locale) {
            ReferenceEntryTranslation preferred = null;
            ReferenceEntryTranslation fallback = null;
            for (var t : entry.getTranslations()) {
                if (locale.equals(t.getLocale())) preferred = t;
                if ("de".equals(t.getLocale())) fallback = t;
            }
            var chosen = preferred != null ? preferred : fallback;
            String label = chosen != null ? chosen.getLabel() : entry.getCode();
            String hint = chosen != null ? chosen.getHint() : null;
            return new ReferenceResponse(entry.getCode(), entry.getSortOrder(),
                    label, hint, entry.getMetadata());
        }
    }
}
