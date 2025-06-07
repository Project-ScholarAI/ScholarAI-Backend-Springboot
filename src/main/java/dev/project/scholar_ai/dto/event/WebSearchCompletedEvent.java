package dev.project.scholar_ai.dto.event;

import dev.project.scholar_ai.dto.paper.metadata.PaperMetadataDto;
import java.util.List;
import java.util.UUID;

public record WebSearchCompletedEvent(UUID projectId, String correlationId, List<PaperMetadataDto> papers) {}
