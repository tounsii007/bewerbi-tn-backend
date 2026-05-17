package tn.bewerbi.domain.favorite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserId(UUID userId);
    Optional<Favorite> findByUserIdAndJobId(UUID userId, UUID jobId);
    void deleteByUserIdAndJobId(UUID userId, UUID jobId);
}
