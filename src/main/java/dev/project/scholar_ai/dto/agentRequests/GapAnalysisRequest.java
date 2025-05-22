package dev.project.scholar_ai.dto.agentRequests;

import java.util.UUID;

public record GapAnalysisRequest(
        UUID paperId,
        String summaryText,
        String correlationId
) {}
