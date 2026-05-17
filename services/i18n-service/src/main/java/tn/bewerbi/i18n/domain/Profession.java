package tn.bewerbi.i18n.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "professions")
public class Profession {

    @Id @Column(length = 80, nullable = false) private String code;
    @Column(nullable = false) private boolean regulated;
    @Column(name = "category_hint") private String categoryHint;
    @Column(nullable = false) private String skills = "";

    @OneToMany(mappedBy = "profession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfessionTranslation> translations = new ArrayList<>();

    protected Profession() {}
    public Profession(String code, boolean regulated, String categoryHint, String skills) {
        this.code = code; this.regulated = regulated;
        this.categoryHint = categoryHint; this.skills = skills;
    }

    public String getCode() { return code; }
    public boolean isRegulated() { return regulated; }
    public String getCategoryHint() { return categoryHint; }
    public List<String> getSkills() {
        if (skills == null || skills.isBlank()) return List.of();
        return List.of(skills.split(","));
    }
    public List<ProfessionTranslation> getTranslations() { return translations; }
}
