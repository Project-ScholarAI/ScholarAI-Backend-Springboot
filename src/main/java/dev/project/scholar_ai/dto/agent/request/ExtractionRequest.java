package dev.project.scholar_ai.dto.agent.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionRequest {

    @NotBlank(message = "Correlation ID is required")
    @JsonProperty("correlationId")
    private String correlationId;

    @NotNull(message = "Paper ID is required") @JsonProperty("paperId")
    private UUID paperId;

    @NotBlank(message = "PDF URL is required")
    @JsonProperty("pdfUrl")
    private String pdfUrl;

    @JsonProperty("requestedBy")
    private String requestedBy;
}
