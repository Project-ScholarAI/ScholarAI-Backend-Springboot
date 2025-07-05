package dev.project.scholar_ai.dto.agent.request;

import java.util.UUID;
import lombok.Builder;

@Builder
public record SummarizationRequest(String correlationId, UUID paperId, String content, String requestedBy) {}
