package dev.project.scholar_ai.repository.paper;

import dev.project.scholar_ai.model.paper.content.PaperSection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperSectionRepository extends JpaRepository<PaperSection, UUID> {
    List<PaperSection> findByPaperId(UUID paperId);
} 