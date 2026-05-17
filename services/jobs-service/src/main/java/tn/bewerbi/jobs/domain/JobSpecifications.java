package tn.bewerbi.jobs.domain;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class JobSpecifications {
    private JobSpecifications() {}

    public static Specification<Job> filters(String search, JobCategory category, JobType type,
                                             String location, GermanLevel minGerman, Integer salaryMin) {
        return (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("status"), JobStatus.ACTIVE));
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("location")), like)));
            }
            if (category != null) ps.add(cb.equal(root.get("category"), category));
            if (type != null) ps.add(cb.equal(root.get("type"), type));
            if (location != null && !location.isBlank()) {
                ps.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }
            if (minGerman != null) {
                ps.add(cb.or(cb.isNull(root.get("germanLevel")),
                        cb.greaterThanOrEqualTo(root.get("germanLevel"), minGerman)));
            }
            if (salaryMin != null) {
                ps.add(cb.or(cb.isNull(root.get("salaryMax")),
                        cb.greaterThanOrEqualTo(root.get("salaryMax"), salaryMin)));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
