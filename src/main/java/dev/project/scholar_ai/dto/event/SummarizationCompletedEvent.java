package dev.project.scholar_ai.dto.event;

import java.util.UUID;

/**
 * Event sent by FastAPI summarizer agent to indicate batch processing has completed.
 * Contains counts only; detailed content is persisted via separate calls/messages.
 */
public record SummarizationCompletedEvent(String correlationId, int processed, int failed) {}
