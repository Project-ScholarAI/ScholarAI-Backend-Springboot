package dev.project.scholar_ai.service.websearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.project.scholar_ai.dto.agent.request.WebSearchRequest;
import dev.project.scholar_ai.dto.agent.request.WebSearchRequestDTO;
import dev.project.scholar_ai.dto.agent.response.WebSearchResponseDto;
import dev.project.scholar_ai.dto.event.WebSearchCompletedEvent;
import dev.project.scholar_ai.dto.paper.metadata.PaperMetadataDto;
import dev.project.scholar_ai.messaging.publisher.WebSearchRequestSender;
import dev.project.scholar_ai.model.core.websearch.WebSearchOperation;
import dev.project.scholar_ai.repository.core.websearch.WebSearchOperationRepository;
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
public class WebSearchService {

    private final WebSearchRequestSender webSearchRequestSender;
    private final WebSearchOperationRepository webSearchOperationRepository;
    private final PaperPersistenceService paperPersistenceService;
    private final ObjectMapper objectMapper;

    @Transactional(transactionManager = "transactionManager")
    public WebSearchResponseDto initiateWebSearch(WebSearchRequestDTO requestDto) {
        String correlationId = UUID.randomUUID().toString();

        log.info(
                "Initiating web search - Project ID: {}, Correlation ID: {}, Query: {}, Domain: {}, Batch Size: {}",
                requestDto.projectId(),
                correlationId,
                requestDto.queryTerms(),
                requestDto.domain(),
                requestDto.batchSize());

        try {
            // Convert query terms to JSON string
            String queryTermsJson = objectMapper.writeValueAsString(requestDto.queryTerms());

            // Create and save web search operation
            WebSearchOperation operation = WebSearchOperation.builder()
                    .correlationId(correlationId)
                    .projectId(requestDto.projectId())
                    .queryTerms(queryTermsJson)
                    .domain(requestDto.domain())
                    .batchSize(requestDto.batchSize())
                    .status(WebSearchOperation.SearchStatus.SUBMITTED)
                    .submittedAt(LocalDateTime.now())
                    .build();

            webSearchOperationRepository.save(operation);

            // Create and send the web search request
            WebSearchRequest webSearchRequest = new WebSearchRequest(
                    requestDto.projectId(),
                    requestDto.queryTerms(),
                    requestDto.domain(),
                    requestDto.batchSize(),
                    correlationId);

            webSearchRequestSender.send(webSearchRequest);

            // Return response DTO
            return new WebSearchResponseDto(
                    requestDto.projectId().toString(),
                    correlationId,
                    requestDto.queryTerms(),
                    requestDto.domain(),
                    requestDto.batchSize(),
                    operation.getStatus().name(),
                    operation.getSubmittedAt(),
                    "Web search job submitted successfully. Results will be available shortly.",
                    List.of() // Empty initially
                    );

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize query terms for correlation ID: {}", correlationId, e);
            throw new RuntimeException("Failed to process search request", e);
        }
    }

    @Transactional(transactionManager = "transactionManager")
    public void updateSearchResults(WebSearchCompletedEvent event) {
        String correlationId = event.correlationId();

        log.debug("Updating search results for correlation ID: {}", correlationId);

        WebSearchOperation operation =
                webSearchOperationRepository.findByCorrelationId(correlationId).orElse(null);

        if (operation != null) {
            // Update operation status
            operation.markAsCompleted(event.papers().size());
            webSearchOperationRepository.save(operation);

            log.info(
                    "Updated search results for correlation ID: {} with {} papers",
                    correlationId,
                    event.papers().size());
        } else {
            log.warn("No existing search operation found for correlation ID: {}", correlationId);
        }
    }

    @Transactional(transactionManager = "transactionManager")
    public void markSearchAsFailed(String correlationId, String errorMessage) {
        webSearchOperationRepository
                .findByCorrelationId(correlationId)
                .ifPresentOrElse(
                        operation -> {
                            operation.markAsFailed(errorMessage);
                            webSearchOperationRepository.save(operation);
                            log.info("Marked search as failed for correlation ID: {}", correlationId);
                        },
                        () -> log.warn("No search operation found for correlation ID: {}", correlationId));
    }

    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public WebSearchResponseDto getSearchResults(String correlationId) {
        return webSearchOperationRepository
                .findByCorrelationId(correlationId)
                .map(operation -> convertToResponseDto(operation, true))
                .orElse(null);
    }

    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<WebSearchResponseDto> getAllSearchResults() {
        return webSearchOperationRepository.findAll().stream()
                .map(operation -> convertToResponseDto(operation, false))
                .toList();
    }

    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<WebSearchResponseDto> getSearchResultsByProject(UUID projectId) {
        return webSearchOperationRepository.findByProjectIdOrderBySubmittedAtDesc(projectId).stream()
                .map(operation -> convertToResponseDto(operation, false))
                .toList();
    }

    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<WebSearchResponseDto> getInProgressSearches() {
        return webSearchOperationRepository.findInProgressOperations().stream()
                .map(operation -> convertToResponseDto(operation, false))
                .toList();
    }

    private WebSearchResponseDto convertToResponseDto(WebSearchOperation operation) {
        return convertToResponseDto(operation, false);
    }

    private WebSearchResponseDto convertToResponseDto(WebSearchOperation operation, boolean includePapers) {
        try {
            // Parse query terms from JSON
            List<String> queryTerms = objectMapper.readValue(
                    operation.getQueryTerms(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

            // Get papers if requested and operation is completed
            List<PaperMetadataDto> papers = List.of();
            if (includePapers && operation.isCompleted()) {
                papers = paperPersistenceService.findPaperDtosByCorrelationId(operation.getCorrelationId());
            }

            return new WebSearchResponseDto(
                    operation.getProjectId().toString(),
                    operation.getCorrelationId(),
                    queryTerms,
                    operation.getDomain(),
                    operation.getBatchSize(),
                    operation.getStatus().name(),
                    operation.getSubmittedAt(),
                    generateStatusMessage(operation),
                    papers);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse query terms for correlation ID: {}", operation.getCorrelationId(), e);
            throw new RuntimeException("Failed to convert operation to DTO", e);
        }
    }

    private String generateStatusMessage(WebSearchOperation operation) {
        return switch (operation.getStatus()) {
            case SUBMITTED -> "Web search job submitted successfully. Results will be available shortly.";
            case IN_PROGRESS -> "Web search is currently in progress. Please check back soon.";
            case COMPLETED -> String.format(
                    "Web search completed successfully! Found %d papers.",
                    operation.getTotalPapersFound() != null ? operation.getTotalPapersFound() : 0);
            case FAILED -> "Web search failed: "
                    + (operation.getErrorMessage() != null ? operation.getErrorMessage() : "Unknown error");
            case CANCELLED -> "Web search was cancelled.";
        };
    }
}
