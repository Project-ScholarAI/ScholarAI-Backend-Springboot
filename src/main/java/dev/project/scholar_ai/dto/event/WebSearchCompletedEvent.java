package dev.project.scholar_ai.dto.event;

import java.util.List;
import java.util.UUID;

public record WebSearchCompletedEvent(UUID projectId, List<EnhancedPaperMetadata> papers, String correlationId) {}
