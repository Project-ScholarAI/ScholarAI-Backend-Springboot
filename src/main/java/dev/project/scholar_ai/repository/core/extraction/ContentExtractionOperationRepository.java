package dev.project.scholar_ai.repository.core.extraction;

import dev.project.scholar_ai.model.core.extraction.ContentExtractionOperation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentExtractionOperationRepository extends JpaRepository<ContentExtractionOperation, String> {

    Optional<ContentExtractionOperation> findByCorrelationId(String correlationId);

    List<ContentExtractionOperation> findByProjectIdOrderBySubmittedAtDesc(UUID projectId);

    @Query("SELECT c FROM ContentExtractionOperation c WHERE c.status IN ('SUBMITTED', 'IN_PROGRESS')")
    List<ContentExtractionOperation> findInProgressOperations();

    List<ContentExtractionOperation> findBySourceCorrelationId(String sourceCorrelationId);

    @Query("SELECT c FROM ContentExtractionOperation c WHERE c.projectId = :projectId AND c.status = :status")
    List<ContentExtractionOperation> findByProjectIdAndStatus(@Param("projectId") UUID projectId, 
                                                               @Param("status") ContentExtractionOperation.ExtractionStatus status);
} 