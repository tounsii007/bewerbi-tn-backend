package tn.bewerbi.i18n.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfessionRepository extends JpaRepository<Profession, String> {
    @Query("""
            select p from Profession p
            left join fetch p.translations t
            where t.locale = :locale
              and lower(t.label) like lower(concat('%', :q, '%'))
            order by t.label
            """)
    List<Profession> searchByLocale(String q, String locale);
}
