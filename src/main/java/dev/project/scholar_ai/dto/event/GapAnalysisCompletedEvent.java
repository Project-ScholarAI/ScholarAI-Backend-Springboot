package dev.project.scholar_ai.dto.event;

import java.util.List;
import java.util.UUID;

public record GapAnalysisCompletedEvent(UUID paperId, List<String> identifiedGaps, String correlationId) {}
