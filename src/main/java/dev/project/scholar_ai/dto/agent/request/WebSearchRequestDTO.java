package dev.project.scholar_ai.dto.agent.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request to trigger web search for academic papers")
public record WebSearchRequestDTO(
        @Schema(
                        description = "Search terms/keywords for finding papers",
                        example = "[\"machine learning\", \"neural networks\", \"optimization\"]")
                @NotEmpty(message = "Query terms cannot be empty")
                List<String> queryTerms,
        @Schema(
                        description = "Academic domain/field of study",
                        example = "Computer Science",
                        allowableValues = {
                            "Computer Science",
                            "Mathematics",
                            "Physics",
                            "Biology",
                            "Chemistry",
                            "Medicine",
                            "Engineering",
                            "Psychology",
                            "Economics"
                        })
                @NotBlank(message = "Domain cannot be blank")
                String domain,
        @Schema(description = "Number of papers to fetch (1-50)", example = "10", minimum = "1", maximum = "50")
                @Min(value = 1, message = "Batch size must be at least 1")
                @Max(value = 50, message = "Batch size cannot exceed 50")
                Integer batchSize) {}
