package dev.project.scholar_ai.repository.core.project;

import dev.project.scholar_ai.model.core.project.ProjectNote;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectNoteRepository extends JpaRepository<ProjectNote, UUID> {

    List<ProjectNote> findByProjectIdOrderByUpdatedAtDesc(UUID projectId);

    List<ProjectNote> findByProjectIdAndIsFavoriteOrderByUpdatedAtDesc(UUID projectId, Boolean isFavorite);

    Optional<ProjectNote> findByIdAndProjectId(UUID id, UUID projectId);

    @Query(
            value =
                    "SELECT * FROM project_notes WHERE project_id = :projectId AND :tag = ANY(tags) ORDER BY updated_at DESC",
            nativeQuery = true)
    List<ProjectNote> findByProjectIdAndTag(@Param("projectId") UUID projectId, @Param("tag") String tag);

    @Query(
            value =
                    "SELECT * FROM project_notes WHERE project_id = :projectId AND (LOWER(title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY updated_at DESC",
            nativeQuery = true)
    List<ProjectNote> findByProjectIdAndSearchTerm(
            @Param("projectId") UUID projectId, @Param("searchTerm") String searchTerm);
}
