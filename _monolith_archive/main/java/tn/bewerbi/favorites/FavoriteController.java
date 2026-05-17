package tn.bewerbi.favorites;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.api.CurrentUser;
import tn.bewerbi.domain.favorite.Favorite;
import tn.bewerbi.domain.favorite.FavoriteRepository;

@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "Favorites")
@PreAuthorize("isAuthenticated()")
public class FavoriteController {

    private final FavoriteService service;

    public FavoriteController(FavoriteService service) { this.service = service; }

    @GetMapping
    public List<UUID> list() { return service.list(CurrentUser.id()); }

    @PostMapping("/{jobId}")
    public void add(@PathVariable UUID jobId) { service.add(CurrentUser.id(), jobId); }

    @DeleteMapping("/{jobId}")
    public void remove(@PathVariable UUID jobId) { service.remove(CurrentUser.id(), jobId); }

    @Service
    @Transactional
    public static class FavoriteService {
        private final FavoriteRepository repo;
        public FavoriteService(FavoriteRepository repo) { this.repo = repo; }

        @Transactional(readOnly = true)
        public List<UUID> list(UUID userId) {
            return repo.findByUserId(userId).stream().map(Favorite::getJobId).toList();
        }

        public void add(UUID userId, UUID jobId) {
            if (repo.findByUserIdAndJobId(userId, jobId).isPresent()) return;
            repo.save(new Favorite(userId, jobId));
        }

        public void remove(UUID userId, UUID jobId) {
            repo.deleteByUserIdAndJobId(userId, jobId);
        }
    }
}
