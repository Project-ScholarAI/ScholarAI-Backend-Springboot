package dev.project.scholar_ai.dto.event.web.search;

import dev.project.scholar_ai.dto.paper.metadata.PaperMetadataDto;
import java.util.List;
import java.util.UUID;

public record PaperFetchCompletedEvent(UUID projectId, List<PaperMetadataDto> papers, String correlationId) {}
