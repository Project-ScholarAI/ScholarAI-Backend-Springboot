package dev.project.scholar_ai.dto.event;

import java.util.List;
import java.util.UUID;

public record PaperFetchCompletedEvent(UUID projectId, List<PaperMetadata> papers, String correlationId) {}
