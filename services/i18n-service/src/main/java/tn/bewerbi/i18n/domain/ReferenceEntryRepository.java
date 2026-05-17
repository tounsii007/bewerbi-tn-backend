package tn.bewerbi.i18n.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReferenceEntryRepository extends JpaRepository<ReferenceEntry, ReferenceEntry.Id> {
    @Query("""
            select e from ReferenceEntry e
            where e.type = :type
            order by e.sortOrder
            """)
    List<ReferenceEntry> listByType(String type);
}
