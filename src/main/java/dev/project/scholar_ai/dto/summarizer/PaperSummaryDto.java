package dev.project.scholar_ai.dto.summarizer;

import java.util.UUID;

public record PaperSummaryDto(UUID paperId, String summaryJson) {} 