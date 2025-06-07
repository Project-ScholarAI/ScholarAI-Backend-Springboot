package dev.project.scholar_ai.repository.paper;

import dev.project.scholar_ai.model.paper.metadata.Paper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperRepository extends JpaRepository<Paper, UUID> {

    List<Paper> findByProjectId(UUID projectId);

    List<Paper> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT p FROM Paper p JOIN p.authors a WHERE a.name LIKE %:author%")
    List<Paper> findByAuthorNameContainingIgnoreCase(@Param("author") String author);

    List<Paper> findByPublicationDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT p FROM Paper p WHERE p.title LIKE %:keyword% OR p.abstractText LIKE %:keyword%")
    List<Paper> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT p FROM Paper p JOIN p.venue v WHERE v.venueName LIKE %:venue%")
    List<Paper> findByVenueNameContainingIgnoreCase(@Param("venue") String venue);

    List<Paper> findByDoi(String doi);

    List<Paper> findBySemanticScholarId(String semanticScholarId);
}
