package tn.bewerbi.jobs.domain;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {
    Page<Job> findByCompanyId(UUID companyId, Pageable pageable);
    Page<Job> findByStatus(JobStatus status, Pageable pageable);
}
