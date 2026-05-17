package tn.bewerbi.i18n.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.i18n.domain.Message;
import tn.bewerbi.i18n.domain.MessageRepository;

/**
 * Admin interface to edit localization at runtime.  Every mutation invalidates
 * the per-locale cache so the change propagates to downstream services as
 * soon as their own Redis entries expire (max 5 minutes).
 */
@RestController
@RequestMapping("/api/v1/admin/i18n")
@Tag(name = "i18n: Admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMessagesController {

    private final AdminService service;
    public AdminMessagesController(AdminService service) { this.service = service; }

    @GetMapping("/messages")
    @Operation(summary = "List all keys in a given locale + namespace")
    public List<MessageDto> list(
            @RequestParam(defaultValue = "de") String locale,
            @RequestParam(defaultValue = "default") String namespace) {
        return service.list(locale, namespace);
    }

    @PutMapping("/messages")
    @Operation(summary = "Upsert a single message; invalidates its locale cache")
    public MessageDto upsert(@Valid @RequestBody UpsertRequest req) {
        return service.upsert(req);
    }

    @PostMapping("/messages/batch")
    @Operation(summary = "Upsert many messages at once")
    public int upsertBatch(@RequestBody List<@Valid UpsertRequest> batch) {
        batch.forEach(service::upsert);
        return batch.size();
    }

    @DeleteMapping("/messages")
    public void delete(@RequestParam String locale,
                       @RequestParam(defaultValue = "default") String namespace,
                       @RequestParam String key) {
        service.delete(locale, namespace, key);
    }

    public record UpsertRequest(
            @NotBlank String locale,
            String namespace,
            @NotBlank String key,
            @NotBlank String value) {}

    public record MessageDto(String locale, String namespace, String key,
                             String value, Instant updatedAt) {}

    @Service
    @Transactional
    public static class AdminService {
        private final MessageRepository repo;
        private final CacheManager caches;

        public AdminService(MessageRepository repo, CacheManager caches) {
            this.repo = repo;
            this.caches = caches;
        }

        @Transactional(readOnly = true)
        public List<MessageDto> list(String locale, String namespace) {
            return repo.findByLocaleAndNamespace(locale, namespace).stream()
                    .map(m -> new MessageDto(m.getLocale(), m.getNamespace(),
                            m.getKey(), m.getValue(), m.getUpdatedAt()))
                    .toList();
        }

        public MessageDto upsert(UpsertRequest r) {
            String ns = r.namespace() == null || r.namespace().isBlank() ? "default" : r.namespace();
            var id = new Message.Id(r.locale(), ns, r.key());
            Message m = repo.findById(id).orElseGet(() -> new Message(r.locale(), ns, r.key(), r.value()));
            m.setValue(r.value());
            repo.save(m);
            invalidate(r.locale(), ns);
            return new MessageDto(m.getLocale(), m.getNamespace(), m.getKey(),
                    m.getValue(), m.getUpdatedAt());
        }

        public void delete(String locale, String namespace, String key) {
            repo.deleteById(new Message.Id(locale, namespace, key));
            invalidate(locale, namespace);
        }

        private void invalidate(String locale, String namespace) {
            var cache = caches.getCache("messages");
            if (cache != null) cache.evict(locale + ":" + namespace);
        }
    }
}
