package dev.project.scholar_ai.dto.agent.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResult {

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("paperId")
    private UUID paperId;

    @JsonProperty("extractedText")
    private String extractedText;

    @JsonProperty("status")
    private String status;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("extractionMethod")
    private String extractionMethod;

    @JsonProperty("textLength")
    private Integer textLength;

    @JsonProperty("handler")
    private String handler;

    @JsonProperty("processing_time")
    private String processingTime;
}
