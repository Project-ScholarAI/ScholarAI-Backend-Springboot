package dev.project.scholar_ai.dto.agent.request;

import java.util.List;
import java.util.UUID;

public record WebSearchRequest(
        UUID projectId, List<String> queryTerms, String domain, Integer batchSize, String correlationId) {}
