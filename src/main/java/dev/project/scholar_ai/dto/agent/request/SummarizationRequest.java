package dev.project.scholar_ai.dto.agent.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record SummarizationRequest(
        String correlationId,
        UUID paperId,
        String content,
        String requestedBy
) {}
