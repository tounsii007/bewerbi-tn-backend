package tn.bewerbi.i18n.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "messages")
@IdClass(Message.Id.class)
public class Message {

    @jakarta.persistence.Id @Column(nullable = false, length = 8) private String locale;
    @jakarta.persistence.Id @Column(nullable = false, length = 40) private String namespace;
    @jakarta.persistence.Id @Column(nullable = false, length = 120) private String key;

    @Column(nullable = false, columnDefinition = "text") private String value;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();

    protected Message() {}
    public Message(String locale, String namespace, String key, String value) {
        this.locale = locale; this.namespace = namespace; this.key = key; this.value = value;
    }

    public String getLocale() { return locale; }
    public String getNamespace() { return namespace; }
    public String getKey() { return key; }
    public String getValue() { return value; }
    public void setValue(String v) { this.value = v; this.updatedAt = Instant.now(); }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Id implements Serializable {
        public String locale; public String namespace; public String key;
        public Id() {}
        public Id(String locale, String namespace, String key) {
            this.locale = locale; this.namespace = namespace; this.key = key;
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id id)) return false;
            return Objects.equals(locale, id.locale)
                    && Objects.equals(namespace, id.namespace)
                    && Objects.equals(key, id.key);
        }
        @Override public int hashCode() { return Objects.hash(locale, namespace, key); }
    }
}
