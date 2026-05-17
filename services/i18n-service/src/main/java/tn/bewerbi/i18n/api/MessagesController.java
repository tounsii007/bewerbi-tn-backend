package tn.bewerbi.i18n.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.bewerbi.i18n.domain.Message;
import tn.bewerbi.i18n.domain.MessageRepository;

@RestController
@RequestMapping("/api/v1/i18n")
@Tag(name = "i18n: Messages")
public class MessagesController {

    private final MessagesService service;
    public MessagesController(MessagesService service) { this.service = service; }

    @GetMapping("/messages")
    @Operation(summary = "Return all key/value pairs for a locale and namespace (default 'default').")
    public Map<String, String> bundle(
            @RequestParam(defaultValue = "de") String locale,
            @RequestParam(defaultValue = "default") String namespace) {
        return service.bundle(locale, namespace);
    }

    @Service
    public static class MessagesService {
        private final MessageRepository messages;
        public MessagesService(MessageRepository messages) { this.messages = messages; }

        @Cacheable(value = "messages", key = "#locale + ':' + #namespace")
        public Map<String, String> bundle(String locale, String namespace) {
            Map<String, String> map = new HashMap<>();
            for (Message m : messages.findByLocaleAndNamespace(locale, namespace)) {
                map.put(m.getKey(), m.getValue());
            }
            // Fall back to the default locale for missing keys
            if (!"de".equals(locale)) {
                for (Message m : messages.findByLocaleAndNamespace("de", namespace)) {
                    map.putIfAbsent(m.getKey(), m.getValue());
                }
            }
            return map;
        }
    }
}
