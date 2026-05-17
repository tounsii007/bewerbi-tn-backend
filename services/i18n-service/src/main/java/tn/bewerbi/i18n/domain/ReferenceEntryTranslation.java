package tn.bewerbi.i18n.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "reference_entry_translations")
@IdClass(ReferenceEntryTranslation.Id.class)
public class ReferenceEntryTranslation {

    @jakarta.persistence.Id @Column(length = 40, nullable = false) private String type;
    @jakarta.persistence.Id @Column(length = 80, nullable = false) private String code;
    @jakarta.persistence.Id @Column(length = 8, nullable = false) private String locale;

    @Column(nullable = false, columnDefinition = "text") private String label;
    @Column(columnDefinition = "text") private String hint;

    protected ReferenceEntryTranslation() {}

    public String getType() { return type; }
    public String getCode() { return code; }
    public String getLocale() { return locale; }
    public String getLabel() { return label; }
    public String getHint() { return hint; }

    public static class Id implements Serializable {
        public String type; public String code; public String locale;
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id id)) return false;
            return Objects.equals(type, id.type) && Objects.equals(code, id.code)
                    && Objects.equals(locale, id.locale);
        }
        @Override public int hashCode() { return Objects.hash(type, code, locale); }
    }
}
