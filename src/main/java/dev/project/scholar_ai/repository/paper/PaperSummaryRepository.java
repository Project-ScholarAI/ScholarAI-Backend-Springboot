package dev.project.scholar_ai.repository.paper;

import dev.project.scholar_ai.model.paper.content.PaperSummary;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaperSummaryRepository extends JpaRepository<PaperSummary, UUID> {} 