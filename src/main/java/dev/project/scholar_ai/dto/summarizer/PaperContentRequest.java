package dev.project.scholar_ai.dto.summarizer;

import java.util.List;
import java.util.UUID;

public record PaperContentRequest(
        UUID paperId,
        List<PaperSectionDto> sections,
        List<PaperCitationDto> citations,
        String summaryJson) {} 