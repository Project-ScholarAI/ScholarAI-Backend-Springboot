package dev.project.scholar_ai.dto.agent.request;

import java.util.List;
import java.util.UUID;

/**
 * Message payload sent from Spring Boot to the FastAPI summarizer agent.
 *
 * @param projectId      Project ID associated with the summarization request
 * @param correlationId  Correlation ID identifying the originating search operation
 * @param paperIds       List of paper IDs to process. If empty, the agent should process
 *                       all papers associated with the correlation ID.
 */
public record SummarizationRequest(UUID projectId, String correlationId, List<UUID> paperIds) {}
