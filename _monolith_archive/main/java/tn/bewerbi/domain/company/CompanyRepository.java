package tn.bewerbi.domain.company;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findBySlug(String slug);
    Page<Company> findByVerificationStatus(VerificationStatus status, Pageable pageable);
    boolean existsBySlug(String slug);
}
