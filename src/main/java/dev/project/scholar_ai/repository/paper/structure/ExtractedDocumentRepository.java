package dev.project.scholar_ai.repository.paper.structure;

import dev.project.scholar_ai.model.paper.structure.ExtractedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExtractedDocumentRepository extends JpaRepository<ExtractedDocument, UUID> {

    /**
     * Find extracted document by paper ID
     */
    Optional<ExtractedDocument> findByPaperId(UUID paperId);

    /**
     * Find all papers that have extracted documents
     */
    @Query("SELECT ed FROM ExtractedDocument ed ORDER BY ed.createdAt DESC")
    List<ExtractedDocument> findAllOrderByCreatedAtDesc();

    /**
     * Find extracted documents with sections
     */
    @Query("SELECT ed FROM ExtractedDocument ed WHERE SIZE(ed.sections) > 0 ORDER BY ed.createdAt DESC")
    List<ExtractedDocument> findWithSections();

    /**
     * Count extracted documents by correlation ID
     */
    @Query("SELECT COUNT(ed) FROM ExtractedDocument ed WHERE ed.paper.correlationId = :correlationId")
    long countByCorrelationId(@Param("correlationId") String correlationId);

    /**
     * Check if extracted document exists for paper
     */
    boolean existsByPaperId(UUID paperId);

    /**
     * Delete extracted document by paper ID
     */
    void deleteByPaperId(UUID paperId);

    /**
     * Find extracted documents with minimum section count
     */
    @Query("SELECT ed FROM ExtractedDocument ed WHERE SIZE(ed.sections) >= :minSections ORDER BY ed.createdAt DESC")
    List<ExtractedDocument> findWithMinimumSections(@Param("minSections") int minSections);
}