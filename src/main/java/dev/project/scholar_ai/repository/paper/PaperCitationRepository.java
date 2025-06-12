package dev.project.scholar_ai.repository.paper;

import dev.project.scholar_ai.model.paper.content.PaperCitation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperCitationRepository extends JpaRepository<PaperCitation, UUID> {
    List<PaperCitation> findByCitingPaperId(UUID citingPaperId);
} 