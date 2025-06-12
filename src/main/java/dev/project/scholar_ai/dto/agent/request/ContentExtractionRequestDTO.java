package dev.project.scholar_ai.dto.agent.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Request to extract content from papers in a websearch operation")
public record ContentExtractionRequestDTO(
        @Schema(description = "Project ID that owns the papers", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "Project ID cannot be null") 
        UUID projectId,
        
        @Schema(description = "Correlation ID from websearch operation containing the papers", 
                example = "websearch-corr-123-456")
        @NotBlank(message = "Source correlation ID cannot be blank") 
        String sourceCorrelationId
) {} 