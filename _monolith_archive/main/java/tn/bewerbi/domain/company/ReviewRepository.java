package tn.bewerbi.domain.company;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Page<Review> findByCompanyId(UUID companyId, Pageable pageable);

    @Query("""
            select coalesce(avg(r.rating), 0), count(r)
            from Review r where r.companyId = :companyId
            """)
    Object[] aggregateByCompany(UUID companyId);
}
