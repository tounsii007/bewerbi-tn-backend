package tn.bewerbi.jobs.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.*;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import tn.bewerbi.common.api.exception.ResourceNotFoundException;
import tn.bewerbi.jobs.domain.JobCategory;

@RestController
@RequestMapping("/api/v1/salary-market")
@Tag(name = "Salary Market")
public class SalaryMarketController {

    private final MarketRepository repo;
    public SalaryMarketController(MarketRepository repo) { this.repo = repo; }

    @GetMapping
    public List<SalaryMarketResponse> all() {
        return repo.findAll().stream().map(SalaryMarketResponse::from).toList();
    }

    @GetMapping("/{category}")
    public SalaryMarketResponse byCategory(@PathVariable JobCategory category) {
        return repo.findById(category)
                .map(SalaryMarketResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("SalaryMarket", category));
    }

    public record SalaryMarketResponse(JobCategory category, int p25Eur, int p50Eur, int p75Eur) {
        static SalaryMarketResponse from(SalaryMarket m) {
            return new SalaryMarketResponse(m.category, m.p25Eur, m.p50Eur, m.p75Eur);
        }
    }

    @Entity
    @Table(name = "salary_market")
    public static class SalaryMarket {
        @Id @Enumerated(EnumType.STRING) @Column(length = 20) JobCategory category;
        @Column(name = "p25_eur") int p25Eur;
        @Column(name = "p50_eur") int p50Eur;
        @Column(name = "p75_eur") int p75Eur;
    }

    public interface MarketRepository extends JpaRepository<SalaryMarket, JobCategory> {}
}
