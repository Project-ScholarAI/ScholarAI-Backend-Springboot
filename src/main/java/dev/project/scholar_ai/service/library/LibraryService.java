package dev.project.scholar_ai.service.library;

import dev.project.scholar_ai.dto.library.LibraryResponseDto;
import dev.project.scholar_ai.dto.paper.metadata.PaperMetadataDto;
import dev.project.scholar_ai.model.core.websearch.WebSearchOperation;
import dev.project.scholar_ai.repository.core.websearch.WebSearchOperationRepository;
import dev.project.scholar_ai.service.websearch.PaperPersistenceService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryService {

    private final PaperPersistenceService paperPersistenceService;
    private final WebSearchOperationRepository webSearchOperationRepository;

    @Transactional(readOnly = true, transactionManager = "paperTransactionManager")
    public LibraryResponseDto getProjectLibrary(UUID projectId) {
        log.info("Retrieving library for project: {}", projectId);

        // Get all web search operations for this project
        List<WebSearchOperation> searchOperations =
                webSearchOperationRepository.findByProjectIdOrderBySubmittedAtDesc(projectId);

        if (searchOperations.isEmpty()) {
            log.debug("No search operations found for project {}", projectId);
            return createEmptyLibraryResponse(projectId);
        }

        // Extract correlation IDs
        List<String> correlationIds = searchOperations.stream()
                .map(WebSearchOperation::getCorrelationId)
                .toList();

        // Get papers for all correlation IDs
        List<PaperMetadataDto> papers = paperPersistenceService.findPaperDtosByProjectId(projectId);

        // Count completed search operations
        int completedOperations = (int) searchOperations.stream()
                .filter(op -> op.getStatus() == WebSearchOperation.SearchStatus.COMPLETED)
                .count();

        String message = generateLibraryMessage(papers.size(), completedOperations, searchOperations.size());

        log.info(
                "Retrieved library for project {}: {} papers from {} completed operations out of {} total operations",
                projectId,
                papers.size(),
                completedOperations,
                searchOperations.size());

        return new LibraryResponseDto(
                projectId.toString(),
                correlationIds,
                papers.size(),
                completedOperations,
                LocalDateTime.now(),
                message,
                papers);
    }

    private LibraryResponseDto createEmptyLibraryResponse(UUID projectId) {
        return new LibraryResponseDto(
                projectId.toString(),
                List.of(),
                0,
                0,
                LocalDateTime.now(),
                "No search operations found for this project",
                List.of());
    }

    private String generateLibraryMessage(int totalPapers, int completedOperations, int totalOperations) {
        if (totalPapers == 0) {
            return "No papers found in project library";
        }

        if (completedOperations == totalOperations) {
            return String.format(
                    "Project library contains %d papers from %d completed search operations",
                    totalPapers, completedOperations);
        } else {
            return String.format(
                    "Project library contains %d papers from %d completed operations (%d operations still in progress)",
                    totalPapers, completedOperations, totalOperations - completedOperations);
        }
    }
}
