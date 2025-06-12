package dev.project.scholar_ai.dto.agent.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response from content extraction operation")
public record ContentExtractionResponseDto(
        String projectId,
        String correlationId,
        String sourceCorrelationId,
        String status,
        LocalDateTime submittedAt,
        LocalDateTime completedAt,
        Integer totalPapers,
        Integer papersProcessed,
        Integer papersExtracted,
        Integer papersSummarized,
        Integer citationsExtracted,
        String message,
        List<ExtractionResult> results
) {
    public record ExtractionResult(
            String paperId,
            String doi,
            String title,
            String extractionStatus,
            String summaryStatus,
            String errorMessage
    ) {}
} 