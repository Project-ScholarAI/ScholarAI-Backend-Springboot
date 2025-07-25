package dev.project.scholar_ai.repository.core.project;

import dev.project.scholar_ai.model.core.project.ProjectCollaborator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectCollaboratorRepository extends JpaRepository<ProjectCollaborator, UUID> {

    List<ProjectCollaborator> findByProjectId(UUID projectId);

    List<ProjectCollaborator> findByCollaboratorId(UUID collaboratorId);

    Optional<ProjectCollaborator> findByProjectIdAndCollaboratorId(UUID projectId, UUID collaboratorId);

    @Query("SELECT pc.projectId FROM ProjectCollaborator pc WHERE pc.collaboratorId = :collaboratorId")
    List<UUID> findProjectIdsByCollaboratorId(@Param("collaboratorId") UUID collaboratorId);

    boolean existsByProjectIdAndCollaboratorId(UUID projectId, UUID collaboratorId);

    void deleteByProjectIdAndCollaboratorId(UUID projectId, UUID collaboratorId);
}
