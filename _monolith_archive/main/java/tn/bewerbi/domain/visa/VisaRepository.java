package tn.bewerbi.domain.visa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisaRepository extends JpaRepository<VisaCase, UUID> {
    Optional<VisaCase> findByUserId(UUID userId);
}
