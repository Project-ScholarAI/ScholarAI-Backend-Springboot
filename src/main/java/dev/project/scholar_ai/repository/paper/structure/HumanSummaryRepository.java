package dev.project.scholar_ai.repository.paper.structure;

import dev.project.scholar_ai.model.paper.structure.HumanSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HumanSummaryRepository extends JpaRepository<HumanSummary, UUID> {

    /**
     * Find human summary by paper ID
     */
    Optional<HumanSummary> findByPaperId(UUID paperId);

    /**
     * Find all summaries ordered by creation date
     */
    @Query("SELECT hs FROM HumanSummary hs ORDER BY hs.createdAt DESC")
    List<HumanSummary> findAllOrderByCreatedAtDesc();

    /**
     * Find summaries with contributions
     */
    @Query("SELECT hs FROM HumanSummary hs WHERE SIZE(hs.keyContributions) > 0 ORDER BY hs.createdAt DESC")
    List<HumanSummary> findWithContributions();

    /**
     * Find summaries with results
     */
    @Query("SELECT hs FROM HumanSummary hs WHERE SIZE(hs.headlineResults) > 0 ORDER BY hs.createdAt DESC")
    List<HumanSummary> findWithResults();

    /**
     * Find summaries by research area keyword
     */
    @Query("SELECT hs FROM HumanSummary hs WHERE LOWER(hs.problemMotivation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(hs.methodOverview) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<HumanSummary> findByKeyword(@Param("keyword") String keyword);

    /**
     * Count summaries by correlation ID
     */
    @Query("SELECT COUNT(hs) FROM HumanSummary hs WHERE hs.paper.correlationId = :correlationId")
    long countByCorrelationId(@Param("correlationId") String correlationId);

    /**
     * Check if human summary exists for paper
     */
    boolean existsByPaperId(UUID paperId);

    /**
     * Delete human summary by paper ID
     */
    void deleteByPaperId(UUID paperId);

    /**
     * Find summaries with minimum number of contributions
     */
    @Query("SELECT hs FROM HumanSummary hs WHERE SIZE(hs.keyContributions) >= :minContributions ORDER BY hs.createdAt DESC")
    List<HumanSummary> findWithMinimumContributions(@Param("minContributions") int minContributions);

    /**
     * Find summaries that have both contributions and results
     */
    @Query("SELECT hs FROM HumanSummary hs WHERE SIZE(hs.keyContributions) > 0 AND SIZE(hs.headlineResults) > 0 ORDER BY hs.createdAt DESC")
    List<HumanSummary> findCompleteWithContributionsAndResults();
}