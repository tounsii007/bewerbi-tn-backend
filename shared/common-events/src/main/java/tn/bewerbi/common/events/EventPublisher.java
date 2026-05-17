package tn.bewerbi.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper over {@link KafkaTemplate} that serializes events as JSON and
 * logs publication failures without throwing.  Services inject this bean
 * directly; unit tests can swap it for a no-op.
 */
@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public EventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.kafka = kafka;
        this.mapper = mapper;
    }

    /** Publishes the given event as JSON to the topic with an optional partition key. */
    public void publish(String topic, String key, Object event) {
        try {
            String payload = mapper.writeValueAsString(event);
            kafka.send(topic, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) log.warn("Failed to publish {} to {}: {}", event.getClass().getSimpleName(), topic, ex.getMessage());
                    });
        } catch (Exception e) {
            log.warn("Failed to serialize event {}: {}", event.getClass().getSimpleName(), e.getMessage());
        }
    }
}
