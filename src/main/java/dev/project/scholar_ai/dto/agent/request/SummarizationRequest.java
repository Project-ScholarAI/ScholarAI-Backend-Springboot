package dev.project.scholar_ai.dto.agent.request;

import java.util.UUID;

public record SummarizationRequest(UUID paperId, String pdfUrl, String correlationId) {}
