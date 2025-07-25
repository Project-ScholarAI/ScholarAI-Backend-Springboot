package dev.project.scholar_ai.dto.agent.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuringRequest {

    @NotBlank(message = "Correlation ID is required")
    @JsonProperty("correlationId")
    private String correlationId;

    @NotNull(message = "Paper ID is required") @JsonProperty("paperId")
    private UUID paperId;

    @NotBlank(message = "Extracted text is required")
    @JsonProperty("extractedText")
    private String extractedText;

    @JsonProperty("paperMetadata")
    private Map<String, Object> paperMetadata;

    @JsonProperty("requestedBy")
    private String requestedBy;
}
