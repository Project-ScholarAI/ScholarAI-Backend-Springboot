package dev.project.scholar_ai.repository.paper.structure;

import dev.project.scholar_ai.model.paper.structure.StructuredFacts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StructuredFactsRepository extends JpaRepository<StructuredFacts, UUID> {

    /**
     * Find structured facts by paper ID
     */
    Optional<StructuredFacts> findByPaperId(UUID paperId);

    /**
     * Find all structured facts ordered by creation date
     */
    @Query("SELECT sf FROM StructuredFacts sf ORDER BY sf.createdAt DESC")
    List<StructuredFacts> findAllOrderByCreatedAtDesc();

    /**
     * Count structured facts by correlation ID
     */
    @Query("SELECT COUNT(sf) FROM StructuredFacts sf WHERE sf.paper.correlationId = :correlationId")
    long countByCorrelationId(@Param("correlationId") String correlationId);

    /**
     * Check if structured facts exist for paper
     */
    boolean existsByPaperId(UUID paperId);

    /**
     * Delete structured facts by paper ID
     */
    void deleteByPaperId(UUID paperId);

    /**
     * Find structured facts with non-empty facts
     */
    @Query("SELECT sf FROM StructuredFacts sf WHERE sf.facts IS NOT NULL")
    List<StructuredFacts> findWithFacts();
}