package tn.bewerbi.domain.application;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    Page<Application> findByApplicantUserId(UUID userId, Pageable pageable);
    Page<Application> findByJobId(UUID jobId, Pageable pageable);
    Optional<Application> findByJobIdAndApplicantUserId(UUID jobId, UUID userId);
    long countByApplicantUserId(UUID userId);
}
