package dev.project.scholar_ai.repository.core.todo;

import dev.project.scholar_ai.model.core.todo.Todo;
import dev.project.scholar_ai.model.core.todo.enums.TodoCategory;
import dev.project.scholar_ai.model.core.todo.enums.TodoPriority;
import dev.project.scholar_ai.model.core.todo.enums.TodoStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo, String>, JpaSpecificationExecutor<Todo> {

    // Find todos by status
    List<Todo> findByStatusIn(List<TodoStatus> statuses);

    // Find todos by priority
    List<Todo> findByPriorityIn(List<TodoPriority> priorities);

    // Find todos by category
    List<Todo> findByCategoryIn(List<TodoCategory> categories);

    // Find todos by due date range
    List<Todo> findByDueDateBetween(LocalDateTime start, LocalDateTime end);

    // Find overdue todos
    @Query("SELECT t FROM Todo t WHERE t.dueDate < :now AND t.status != 'completed'")
    List<Todo> findOverdueTodos(@Param("now") LocalDateTime now);

    // Find todos due today
    @Query("SELECT t FROM Todo t WHERE DATE(t.dueDate) = DATE(:today)")
    List<Todo> findTodosDueToday(@Param("today") LocalDateTime today);

    // Find todos due this week
    @Query("SELECT t FROM Todo t WHERE t.dueDate BETWEEN :startOfWeek AND :endOfWeek")
    List<Todo> findTodosDueThisWeek(
            @Param("startOfWeek") LocalDateTime startOfWeek, @Param("endOfWeek") LocalDateTime endOfWeek);

    // Search todos by title or description
    @Query("SELECT t FROM Todo t WHERE " + "LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR "
            + "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Todo> searchTodos(@Param("search") String search);

    // Find todos by tags
    @Query("SELECT DISTINCT t FROM Todo t JOIN t.tags tag WHERE tag IN :tags")
    List<Todo> findByTagsIn(@Param("tags") Set<String> tags);

    // Find todos by related project
    List<Todo> findByRelatedProjectId(String projectId);

    // Count todos by status
    long countByStatus(TodoStatus status);

    // Count todos by priority
    long countByPriority(TodoPriority priority);
}
