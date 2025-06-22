package dev.project.scholar_ai.service.websearch;

import dev.project.scholar_ai.dto.paper.metadata.PaperMetadataDto;
import dev.project.scholar_ai.mapping.paper.PaperMapper;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.repository.core.websearch.WebSearchOperationRepository;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaperPersistenceService {

    private final PaperRepository paperRepository;
    private final PaperMapper paperMapper;
    private final WebSearchOperationRepository webSearchOperationRepository;
    private final PaperDeduplicationService paperDeduplicationService;

    @Transactional(transactionManager = "paperTransactionManager")
    public List<Paper> savePapers(List<PaperMetadataDto> paperDtos, String correlationId) {
        log.info("Persisting {} papers for correlation ID {}", paperDtos.size(), correlationId);

        // Filter out duplicate papers before processing
        List<PaperMetadataDto> newPapers = paperDeduplicationService.filterNewPapers(paperDtos);

        int duplicateCount = paperDtos.size() - newPapers.size();
        if (duplicateCount > 0) {
            log.info(
                    "Filtered out {} duplicate papers. Processing {} new papers for correlation ID {}",
                    duplicateCount,
                    newPapers.size(),
                    correlationId);
        }

        if (newPapers.isEmpty()) {
            log.info("No new papers to save for correlation ID {}", correlationId);
            return Collections.emptyList();
        }

        return newPapers.stream()
                .map(dto -> {
                    try {
                        Paper paper = paperMapper.toEntity(dto);
                        paper.setCorrelationId(correlationId);

                        // Set bidirectional relationships
                        if (paper.getAuthors() != null) {
                            paper.getAuthors().forEach(author -> author.setPaper(paper));
                        }

                        if (paper.getExternalIds() != null) {
                            paper.getExternalIds().forEach(externalId -> externalId.setPaper(paper));
                        }

                        if (paper.getVenue() != null) {
                            paper.getVenue().setPaper(paper);
                        }

                        if (paper.getMetrics() != null) {
                            paper.getMetrics().setPaper(paper);
                        }

                        Paper savedPaper = paperRepository.save(paper);
                        log.debug(
                                "Successfully saved new paper: {} (DOI: {})",
                                savedPaper.getTitle(),
                                savedPaper.getDoi());
                        return savedPaper;

                    } catch (Exception e) {
                        log.error(
                                "Failed to save paper: {} (DOI: {}). Error: {}",
                                dto.title(),
                                dto.doi(),
                                e.getMessage(),
                                e);
                        throw new RuntimeException("Failed to save paper: " + dto.title(), e);
                    }
                })
                .toList();
    }

    @Transactional(readOnly = true, transactionManager = "paperTransactionManager")
    public List<Paper> findPapersByCorrelationId(String correlationId) {
        log.debug("Finding papers for correlation ID {}", correlationId);
        return paperRepository.findByCorrelationId(correlationId);
    }

    @Transactional(readOnly = true, transactionManager = "paperTransactionManager")
    public List<PaperMetadataDto> findPaperDtosByCorrelationId(String correlationId) {
        log.debug("Finding paper DTOs for correlation ID {}", correlationId);
        List<Paper> papers = paperRepository.findByCorrelationId(correlationId);
        return papers.stream().map(paperMapper::toDto).toList();
    }

    @Transactional(readOnly = true, transactionManager = "paperTransactionManager")
    public List<Paper> findPapersByProjectId(UUID projectId) {
        log.debug("Finding papers for project {}", projectId);

        // Get all correlation IDs for this project from WebSearchOperations
        List<String> correlationIds =
                webSearchOperationRepository.findByProjectIdOrderBySubmittedAtDesc(projectId).stream()
                        .map(operation -> operation.getCorrelationId())
                        .toList();

        if (correlationIds.isEmpty()) {
            log.debug("No web search operations found for project {}", projectId);
            return Collections.emptyList();
        }

        return paperRepository.findByCorrelationIdIn(correlationIds);
    }

    @Transactional(readOnly = true, transactionManager = "paperTransactionManager")
    public List<PaperMetadataDto> findPaperDtosByProjectId(UUID projectId) {
        log.debug("Finding paper DTOs for project {}", projectId);
        List<Paper> papers = findPapersByProjectId(projectId);
        return papers.stream().map(paperMapper::toDto).toList();
    }
}
