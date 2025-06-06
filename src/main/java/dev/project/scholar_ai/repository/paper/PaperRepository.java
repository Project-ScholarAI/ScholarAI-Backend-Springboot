package dev.project.scholar_ai.repository.paper;

import dev.project.scholar_ai.model.paper.Paper;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {

    List<Paper> findByTitleContainingIgnoreCase(String title);

    List<Paper> findByAuthorsContainingIgnoreCase(String author);

    List<Paper> findByPublicationDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT p FROM Paper p WHERE p.title LIKE %:keyword% OR p.abstractText LIKE %:keyword%")
    List<Paper> searchByKeyword(@Param("keyword") String keyword);

    List<Paper> findByJournalIgnoreCase(String journal);
}
