package tn.bewerbi.i18n.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "profession_translations")
@IdClass(ProfessionTranslation.Id.class)
public class ProfessionTranslation {

    @jakarta.persistence.Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profession_code", referencedColumnName = "code", nullable = false)
    private Profession profession;

    @jakarta.persistence.Id @Column(length = 8, nullable = false) private String locale;

    @Column(nullable = false, columnDefinition = "text") private String label;

    protected ProfessionTranslation() {}
    public ProfessionTranslation(Profession p, String locale, String label) {
        this.profession = p; this.locale = locale; this.label = label;
    }

    public Profession getProfession() { return profession; }
    public String getLocale() { return locale; }
    public String getLabel() { return label; }

    public static class Id implements Serializable {
        public String profession; public String locale;
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id id)) return false;
            return Objects.equals(profession, id.profession) && Objects.equals(locale, id.locale);
        }
        @Override public int hashCode() { return Objects.hash(profession, locale); }
    }
}
