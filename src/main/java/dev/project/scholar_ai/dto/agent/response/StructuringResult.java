package dev.project.scholar_ai.dto.agent.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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
public class StructuringResult {

    @JsonProperty("correlationId")
    private String correlationId;

    @JsonProperty("paperId")
    private UUID paperId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("sections")
    private List<Map<String, Object>> sections;

    @JsonProperty("structuredFacts")
    private Map<String, Object> structuredFacts;

    @JsonProperty("humanSummary")
    private Map<String, Object> humanSummary;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("handler")
    private String handler;

    @JsonProperty("requestedBy")
    private String requestedBy;

    @JsonProperty("textLength")
    private Integer textLength;

    @JsonProperty("sectionsCount")
    private Integer sectionsCount;

    @JsonProperty("processingModel")
    private String processingModel;
}
