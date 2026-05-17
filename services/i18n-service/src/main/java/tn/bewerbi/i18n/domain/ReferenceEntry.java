package tn.bewerbi.i18n.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "reference_entries")
@IdClass(ReferenceEntry.Id.class)
public class ReferenceEntry {

    @jakarta.persistence.Id @Column(length = 40, nullable = false) private String type;
    @jakarta.persistence.Id @Column(length = 80, nullable = false) private String code;

    @Column(name = "sort_order", nullable = false) private int sortOrder;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode metadata;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
            @JoinColumn(name = "type", referencedColumnName = "type"),
            @JoinColumn(name = "code", referencedColumnName = "code")
    })
    private List<ReferenceEntryTranslation> translations = new ArrayList<>();

    protected ReferenceEntry() {}

    public String getType() { return type; }
    public String getCode() { return code; }
    public int getSortOrder() { return sortOrder; }
    public JsonNode getMetadata() { return metadata; }
    public List<ReferenceEntryTranslation> getTranslations() { return translations; }

    public static class Id implements Serializable {
        public String type; public String code;
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id id)) return false;
            return Objects.equals(type, id.type) && Objects.equals(code, id.code);
        }
        @Override public int hashCode() { return Objects.hash(type, code); }
    }
}
