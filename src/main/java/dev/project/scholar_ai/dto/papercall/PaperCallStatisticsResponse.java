package dev.project.scholar_ai.dto.papercall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperCallStatisticsResponse {
    private String domain;
    private int totalCalls;
    private int conferences;
    private int journals;
    private Map<String, Integer> sources;
    private String timestamp;
} 