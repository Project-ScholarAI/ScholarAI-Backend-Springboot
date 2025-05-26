package dev.project.scholar_ai.dto.agentRequests;

import java.util.List;
import java.util.UUID;

public record WebSearchRequest(
        UUID projectId, List<String> queryTerms, String domain, int batchSize, String correlationId) {}
