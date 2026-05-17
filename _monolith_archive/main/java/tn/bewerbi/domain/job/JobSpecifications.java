package tn.bewerbi.domain.job;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import tn.bewerbi.domain.profile.GermanLevel;

public final class JobSpecifications {

    private JobSpecifications() {}

    public static Specification<Job> withFilters(
            String search,
            JobCategory category,
            JobType type,
            String location,
            GermanLevel minGermanLevel,
            Integer salaryMin) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), JobStatus.ACTIVE));
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("location")), like)));
            }
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (type != null) predicates.add(cb.equal(root.get("type"), type));
            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }
            if (minGermanLevel != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("germanLevel")),
                        cb.greaterThanOrEqualTo(root.get("germanLevel"), minGermanLevel)));
            }
            if (salaryMin != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("salaryMax")),
                        cb.greaterThanOrEqualTo(root.get("salaryMax"), salaryMin)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
