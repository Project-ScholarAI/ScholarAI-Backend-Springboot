package dev.project.scholar_ai.repository.core.todo;

import dev.project.scholar_ai.dto.todo.request.TodoFiltersReqDTO;
import dev.project.scholar_ai.model.core.todo.Todo;
import dev.project.scholar_ai.model.core.todo.enums.TodoCategory;
import dev.project.scholar_ai.model.core.todo.enums.TodoPriority;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

public final class TodoSpecification {

    private TodoSpecification() {}

    public static Specification<Todo> fromFilters(TodoFiltersReqDTO filters, String userId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("User ID cannot be null or empty.");
            }
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));

            if (filters == null) {
                return criteriaBuilder.conjunction();
            }

            // Status filter
            if (filters.getStatus() != null && !filters.getStatus().isEmpty()) {
                predicates.add(root.get("status").in(filters.getStatus()));
            }

            // Priority filter
            if (!CollectionUtils.isEmpty(filters.getPriority())) {
                List<TodoPriority> priorities = filters.getPriority().stream()
                        .map(p -> TodoPriority.valueOf(p.toUpperCase()))
                        .collect(Collectors.toList());
                predicates.add(root.get("priority").in(priorities));
            }

            // Category filter
            if (!CollectionUtils.isEmpty(filters.getCategory())) {
                List<TodoCategory> categories = filters.getCategory().stream()
                        .map(c -> TodoCategory.valueOf(c.toUpperCase()))
                        .collect(Collectors.toList());
                predicates.add(root.get("category").in(categories));
            }

            // Due date range filter
            try {
                if (filters.getDueDateStart() != null
                        && !filters.getDueDateStart().isBlank()) {
                    LocalDateTime startDate = LocalDateTime.parse(filters.getDueDateStart());
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), startDate));
                }
                if (filters.getDueDateEnd() != null && !filters.getDueDateEnd().isBlank()) {
                    LocalDateTime endDate = LocalDateTime.parse(filters.getDueDateEnd());
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), endDate));
                }
            } catch (DateTimeParseException e) {
                // Ignoring invalid date formats for now, but could add logging.
            }

            // Project ID filter
            if (filters.getProjectId() != null && !filters.getProjectId().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("relatedProjectId"), filters.getProjectId()));
            }

            // Search query filter (title and description)
            if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
                String searchPattern = "%" + filters.getSearch().toLowerCase() + "%";
                Predicate titlePredicate =
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate descriptionPredicate =
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }

            // Tags filter
            if (!CollectionUtils.isEmpty(filters.getTags())) {
                query.distinct(true); // Ensure distinct results when joining
                predicates.add(root.join("tags").in(filters.getTags()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
