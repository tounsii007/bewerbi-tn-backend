package tn.bewerbi.i18n.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.bewerbi.i18n.domain.Profession;
import tn.bewerbi.i18n.domain.ProfessionRepository;

@RestController
@RequestMapping("/api/v1/professions")
@Tag(name = "i18n: Professions")
public class ProfessionsController {

    private final ProfessionService service;
    public ProfessionsController(ProfessionService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Search professions by label (case-insensitive, locale-aware).")
    public List<ProfessionResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "de") String locale,
            @RequestParam(defaultValue = "8") int limit) {
        return service.search(q, locale, Math.min(Math.max(limit, 1), 50));
    }

    public record ProfessionResponse(
            String code, String label, boolean regulated,
            String categoryHint, List<String> skills) {}

    @Service
    public static class ProfessionService {
        private final ProfessionRepository repo;
        public ProfessionService(ProfessionRepository repo) { this.repo = repo; }

        public List<ProfessionResponse> search(String q, String locale, int limit) {
            if (q == null || q.trim().length() < 2) return List.of();
            return repo.searchByLocale(q.trim(), locale).stream()
                    .limit(limit)
                    .map(p -> new ProfessionResponse(
                            p.getCode(),
                            p.getTranslations().stream()
                                    .filter(t -> t.getLocale().equals(locale))
                                    .map(t -> t.getLabel())
                                    .findFirst()
                                    .orElse(p.getCode()),
                            p.isRegulated(), p.getCategoryHint(), p.getSkills()))
                    .toList();
        }
    }
}
