package tn.bewerbi.domain.favorite;

import jakarta.persistence.*;
import java.util.UUID;
import tn.bewerbi.domain.BaseEntity;

@Entity
@Table(name = "favorites", indexes = {
        @Index(name = "uq_favorites_unique", columnList = "user_id, job_id", unique = true),
        @Index(name = "idx_favorites_user", columnList = "user_id")
})
public class Favorite extends BaseEntity {

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "job_id", nullable = false, columnDefinition = "uuid")
    private UUID jobId;

    protected Favorite() {}

    public Favorite(UUID userId, UUID jobId) {
        this.userId = userId;
        this.jobId = jobId;
    }

    public UUID getUserId() { return userId; }
    public UUID getJobId() { return jobId; }
}
