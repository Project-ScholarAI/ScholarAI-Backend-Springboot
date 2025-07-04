package dev.project.scholar_ai.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.project.scholar_ai.enums.ExtractionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionCompletedEvent {
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    @JsonProperty("paperId")
    private UUID paperId;
    
    @JsonProperty("extractedText")
    private String extractedText;
    
    @JsonProperty("status")
    private ExtractionStatus status;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    @JsonProperty("extractionMethod")
    private String extractionMethod;
    
    @JsonProperty("textLength")
    private Integer textLength;
    
    @JsonProperty("completedAt")
    private LocalDateTime completedAt;
    
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;
}