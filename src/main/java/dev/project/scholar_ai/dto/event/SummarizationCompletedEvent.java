package dev.project.scholar_ai.dto.event;

import java.util.UUID;

public record SummarizationCompletedEvent(
        UUID paperId,
        String summaryText,
        String correlationId
) {}
