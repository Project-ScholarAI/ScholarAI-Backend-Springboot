package dev.project.scholar_ai.dto.agent.request;

import java.util.List;
import java.util.UUID;

public record ContentExtractionRequest(
        UUID projectId,
        String sourceCorrelationId,
        String correlationId,
        List<PaperForExtraction> papers) {
    public record PaperForExtraction(
            UUID paperId,
            String doi,
            String title,
            String pdfContentUrl,
            String pdfUrl) {}
} 