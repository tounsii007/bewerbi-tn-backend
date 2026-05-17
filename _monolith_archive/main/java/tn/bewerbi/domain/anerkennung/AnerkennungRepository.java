package tn.bewerbi.domain.anerkennung;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnerkennungRepository extends JpaRepository<AnerkennungCase, UUID> {
    Optional<AnerkennungCase> findByUserId(UUID userId);
}
