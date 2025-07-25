package dev.project.scholar_ai.service.library;

import dev.project.scholar_ai.dto.paper.metadata.PaperMetadataDto;
import dev.project.scholar_ai.dto.paper.metadata.UploadedPaperRequest;
import dev.project.scholar_ai.mapping.paper.PaperMapper;
import dev.project.scholar_ai.model.core.project.Project;
import dev.project.scholar_ai.model.core.project.ProjectCollaborator;
import dev.project.scholar_ai.model.core.websearch.WebSearchOperation;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.repository.core.project.ProjectCollaboratorRepository;
import dev.project.scholar_ai.repository.core.project.ProjectRepository;
import dev.project.scholar_ai.repository.core.websearch.WebSearchOperationRepository;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadedPaperService {

    private final PaperRepository paperRepository;
    private final PaperMapper paperMapper;
    private final ProjectRepository projectRepository;
    private final ProjectCollaboratorRepository projectCollaboratorRepository;
    private final WebSearchOperationRepository webSearchOperationRepository;

    /**
     * Process and save an uploaded paper to a project's library
     */
    @Transactional(transactionManager = "paperTransactionManager")
    public PaperMetadataDto saveUploadedPaper(UploadedPaperRequest request, UUID userId) {
        UUID projectId = request.projectId();
        log.info("Processing uploaded paper for project: {} by user: {}", projectId, userId);

        // Validate project access
        validateProjectAccess(projectId, userId);

        // Generate a unique correlation ID for this uploaded paper
        String correlationId = "uploaded-" + UUID.randomUUID().toString();

        try {
            // Create web search operation entry for consistency
            createWebSearchOperationEntry(projectId, correlationId, request);

            // Convert UploadedPaperRequest to PaperMetadataDto
            PaperMetadataDto paperDto = convertToPaperMetadataDto(request, correlationId);

            // Convert to Paper entity
            Paper paper = paperMapper.toEntity(paperDto);
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

            // Save the paper
            Paper savedPaper = paperRepository.save(paper);

            log.info(
                    "Successfully saved uploaded paper: {} (ID: {}) with correlation ID: {}",
                    savedPaper.getTitle(),
                    savedPaper.getId(),
                    correlationId);

            // Return the saved paper as DTO
            return paperMapper.toDto(savedPaper);

        } catch (Exception e) {
            log.error("Failed to save uploaded paper for project {}: {}", projectId, e.getMessage(), e);
            throw new RuntimeException("Failed to save uploaded paper: " + e.getMessage(), e);
        }
    }

    /**
     * Create a web search operation entry for uploaded paper consistency
     */
    private void createWebSearchOperationEntry(UUID projectId, String correlationId, UploadedPaperRequest request) {
        try {
            WebSearchOperation operation = WebSearchOperation.builder()
                    .correlationId(correlationId)
                    .projectId(projectId)
                    .queryTerms("[\"Uploaded Paper: " + request.title() + "\"]") // JSON string format
                    .domain("Uploaded")
                    .batchSize(1) // Single paper upload
                    .status(WebSearchOperation.SearchStatus.COMPLETED) // Already completed since it's uploaded
                    .submittedAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .totalPapersFound(1)
                    .searchDurationMs(0L) // Instant upload
                    .build();

            webSearchOperationRepository.save(operation);
            log.debug("Created web search operation entry for uploaded paper: {}", correlationId);

        } catch (Exception e) {
            log.error(
                    "Failed to create web search operation entry for correlation ID {}: {}",
                    correlationId,
                    e.getMessage(),
                    e);
            // Don't throw here - paper upload should still succeed even if web search
            // operation creation fails
        }
    }

    /**
     * Convert UploadedPaperRequest to PaperMetadataDto with proper defaults
     */
    private PaperMetadataDto convertToPaperMetadataDto(UploadedPaperRequest request, String correlationId) {
        // Set defaults for optional fields
        LocalDate publicationDate = request.publicationDate() != null ? request.publicationDate() : LocalDate.now();
        Boolean isOpenAccess = request.isOpenAccess() != null ? request.isOpenAccess() : true;
        List<String> publicationTypes =
                request.publicationTypes() != null ? request.publicationTypes() : List.of("Uploaded Document");
        List<String> fieldsOfStudy = request.fieldsOfStudy() != null ? request.fieldsOfStudy() : new ArrayList<>();

        // Ensure pdfUrl is set to pdfContentUrl if not provided
        String pdfUrl = request.pdfUrl() != null ? request.pdfUrl() : request.pdfContentUrl();

        return new PaperMetadataDto(
                null, // ID will be generated
                request.title(),
                request.abstractText(),
                request.authors() != null ? request.authors() : new ArrayList<>(),
                publicationDate,
                request.doi(),
                request.semanticScholarId(),
                request.externalIds() != null ? request.externalIds() : new java.util.HashMap<>(),
                request.source(),
                request.pdfContentUrl(),
                pdfUrl,
                isOpenAccess,
                request.paperUrl(),
                request.venueName(),
                request.publisher(),
                publicationTypes,
                request.volume(),
                request.issue(),
                request.pages(),
                request.citationCount(),
                request.referenceCount(),
                request.influentialCitationCount(),
                fieldsOfStudy);
    }

    /**
     * Validate that the user has access to the project
     */
    private void validateProjectAccess(UUID projectId, UUID userId) {
        // First try to find as owner
        Project project = projectRepository.findByIdAndUserId(projectId, userId).orElse(null);

        // If not found as owner, check if user is a collaborator
        if (project == null) {
            ProjectCollaborator collaboration = projectCollaboratorRepository
                    .findByProjectIdAndCollaboratorId(projectId, userId)
                    .orElse(null);

            if (collaboration == null) {
                throw new RuntimeException("Project not found or access denied");
            }

            log.debug(
                    "User {} is a collaborator on project {} with role: {}",
                    userId,
                    projectId,
                    collaboration.getRole());
        } else {
            log.debug("User {} is the owner of project {}", userId, projectId);
        }
    }
}
