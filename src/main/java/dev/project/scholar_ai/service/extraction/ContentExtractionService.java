package dev.project.scholar_ai.service.extraction;

import dev.project.scholar_ai.dto.agent.request.ContentExtractionRequest;
import dev.project.scholar_ai.dto.agent.request.ContentExtractionRequestDTO;
import dev.project.scholar_ai.dto.agent.response.ContentExtractionResponseDto;
import dev.project.scholar_ai.messaging.publisher.ContentExtractionRequestSender;
import dev.project.scholar_ai.model.core.extraction.ContentExtractionOperation;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.repository.core.extraction.ContentExtractionOperationRepository;
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
public class ContentExtractionService {

    private final ContentExtractionRequestSender contentExtractionRequestSender;
    private final ContentExtractionOperationRepository contentExtractionOperationRepository;
    private final PaperPersistenceService paperPersistenceService;

    @Transactional(transactionManager = "transactionManager")
    public ContentExtractionResponseDto initiateContentExtraction(ContentExtractionRequestDTO requestDto) {
        String correlationId = "extract-" + UUID.randomUUID().toString();

        log.info(
                "Initiating content extraction - Project ID: {}, Source Correlation ID: {}, New Correlation ID: {}",
                requestDto.projectId(),
                requestDto.sourceCorrelationId(),
                correlationId);

        // STEP 1: Fetch papers from the database using the websearch correlation ID
        List<Paper> papers = paperPersistenceService.findPapersByCorrelationId(requestDto.sourceCorrelationId());
        
        if (papers.isEmpty()) {
            throw new RuntimeException("No papers found for correlation ID: " + requestDto.sourceCorrelationId());
        }

        log.info("Found {} papers to process for correlation ID: {}", papers.size(), requestDto.sourceCorrelationId());

        // STEP 2: Create content extraction operation
        ContentExtractionOperation operation = ContentExtractionOperation.builder()
                .correlationId(correlationId)
                .projectId(requestDto.projectId())
                .sourceCorrelationId(requestDto.sourceCorrelationId())
                .totalPapersToProcess(papers.size())
                .submittedAt(LocalDateTime.now())
                .build();

        contentExtractionOperationRepository.save(operation);

        // STEP 3: Convert papers to extraction format with existing PDF URLs
        List<ContentExtractionRequest.PaperForExtraction> papersForExtraction = papers.stream()
                .map(paper -> new ContentExtractionRequest.PaperForExtraction(
                        paper.getId(),
                        paper.getDoi(),
                        paper.getTitle(),
                        paper.getPdfContentUrl(), // B2 storage URL (preferred)
                        paper.getPdfUrl()         // Original source URL (fallback)
                ))
                .toList();

        // STEP 4: Create and send the content extraction request to FastAPI
        ContentExtractionRequest extractionRequest = new ContentExtractionRequest(
                requestDto.projectId(),
                requestDto.sourceCorrelationId(),
                correlationId,
                papersForExtraction
        );

        contentExtractionRequestSender.send(extractionRequest);

        log.info("Sent content extraction request with {} papers to FastAPI", papersForExtraction.size());

        // STEP 5: Return response DTO
        return new ContentExtractionResponseDto(
                requestDto.projectId().toString(),
                correlationId,
                requestDto.sourceCorrelationId(),
                operation.getStatus().name(),
                operation.getSubmittedAt(),
                null, // completedAt
                papers.size(),
                0, // papersProcessed
                0, // papersExtracted
                0, // papersSummarized
                0, // citationsExtracted
                "Content extraction job submitted successfully. Processing " + papers.size() + " papers.",
                List.of() // Empty initially
        );
    }

    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public ContentExtractionResponseDto getExtractionResults(String correlationId) {
        return contentExtractionOperationRepository
                .findByCorrelationId(correlationId)
                .map(this::convertToResponseDto)
                .orElse(null);
    }

    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<ContentExtractionResponseDto> getAllExtractions() {
        return contentExtractionOperationRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true, transactionManager = "transactionManager")
    public List<ContentExtractionResponseDto> getExtractionsByProject(String projectId) {
        UUID projectUuid = UUID.fromString(projectId);
        return contentExtractionOperationRepository.findByProjectIdOrderBySubmittedAtDesc(projectUuid).stream()
                .map(this::convertToResponseDto)
                .toList();
    }

    private ContentExtractionResponseDto convertToResponseDto(ContentExtractionOperation operation) {
        String message = generateStatusMessage(operation);
        
        return new ContentExtractionResponseDto(
                operation.getProjectId().toString(),
                operation.getCorrelationId(),
                operation.getSourceCorrelationId(),
                operation.getStatus().name(),
                operation.getSubmittedAt(),
                operation.getCompletedAt(),
                operation.getTotalPapersToProcess(),
                operation.getPapersProcessed() != null ? operation.getPapersProcessed() : 0,
                operation.getPapersExtracted() != null ? operation.getPapersExtracted() : 0,
                operation.getPapersSummarized() != null ? operation.getPapersSummarized() : 0,
                operation.getCitationsExtracted() != null ? operation.getCitationsExtracted() : 0,
                message,
                List.of() // TODO: Add detailed results when needed
        );
    }

    private String generateStatusMessage(ContentExtractionOperation operation) {
        return switch (operation.getStatus()) {
            case SUBMITTED -> "Content extraction job submitted successfully. Processing will begin shortly.";
            case IN_PROGRESS -> String.format(
                    "Content extraction in progress. Processed %d/%d papers (%.1f%% complete).",
                    operation.getPapersProcessed() != null ? operation.getPapersProcessed() : 0,
                    operation.getTotalPapersToProcess() != null ? operation.getTotalPapersToProcess() : 0,
                    operation.getCompletionPercentage());
            case COMPLETED -> String.format(
                    "Content extraction completed successfully! Processed %d papers, extracted %d, summarized %d, found %d citations.",
                    operation.getPapersProcessed() != null ? operation.getPapersProcessed() : 0,
                    operation.getPapersExtracted() != null ? operation.getPapersExtracted() : 0,
                    operation.getPapersSummarized() != null ? operation.getPapersSummarized() : 0,
                    operation.getCitationsExtracted() != null ? operation.getCitationsExtracted() : 0);
            case PARTIALLY_COMPLETED -> String.format(
                    "Content extraction partially completed. Processed %d papers with some errors: %s",
                    operation.getPapersProcessed() != null ? operation.getPapersProcessed() : 0,
                    operation.getErrorMessage() != null ? operation.getErrorMessage() : "Unknown errors");
            case FAILED -> "Content extraction failed: " 
                    + (operation.getErrorMessage() != null ? operation.getErrorMessage() : "Unknown error");
            case CANCELLED -> "Content extraction was cancelled.";
        };
    }
} 