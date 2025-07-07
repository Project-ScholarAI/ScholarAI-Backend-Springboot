package dev.project.scholar_ai.service.extraction;

import dev.project.scholar_ai.dto.agent.response.ExtractionResult;
import dev.project.scholar_ai.enums.ExtractionStatus;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import dev.project.scholar_ai.service.structuring.StructuringService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractionService {

    private final PaperRepository paperRepository;
    private final StructuringService structuringService;

    /**
     * Updates a paper with extracted text content from the extraction result.
     *
     * @param result The extraction result containing paper ID and extracted text
     */
    @Transactional(transactionManager = "paperTransactionManager")
    public void updatePaperWithExtractedText(ExtractionResult result) {
        log.info("Updating paper {} with extraction results", result.getPaperId());

        try {
            Paper paper = paperRepository
                    .findById(result.getPaperId())
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + result.getPaperId()));

            // Sanitize and update paper with extracted text
            String sanitizedText = sanitizeText(result.getExtractedText());
            paper.setExtractedText(sanitizedText);
            paper.setExtractionStatus(ExtractionStatus.valueOf(result.getStatus()));
            paper.setExtractedAt(LocalDateTime.now());

            // Save the updated paper
            paperRepository.save(paper);

            log.info(
                    "Successfully updated paper {} with extracted text (length: {} characters)",
                    result.getPaperId(),
                    result.getTextLength());

        } catch (Exception e) {
            log.error("Failed to update paper {} with extracted text: {}", result.getPaperId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update paper with extracted text", e);
        }
    }

    /**
     * Triggers text structuring for a paper with extracted text.
     *
     * @param paperId The ID of the paper to structure
     * @param extractedText The extracted text content
     */
    public void triggerStructuring(UUID paperId, String extractedText) {
        try {
            log.info("Triggering text structuring for paper: {}", paperId);

            // Trigger structuring which will later trigger summarization
            structuringService.triggerStructuring(paperId, extractedText, "extraction-service");

            log.info("Text structuring request sent for paper: {}", paperId);

        } catch (Exception e) {
            log.error("Failed to trigger text structuring for paper {}: {}", paperId, e.getMessage(), e);
            // Don't throw here - extraction was successful, structuring failure shouldn't break the flow
        }
    }

    /**
     * Gets the extraction status for a paper.
     *
     * @param paperId The ID of the paper
     * @return The extraction status
     */
    public ExtractionStatus getExtractionStatus(UUID paperId) {
        return paperRepository.findById(paperId).map(Paper::getExtractionStatus).orElse(ExtractionStatus.PENDING);
    }

    /**
     * Initiates text extraction for a paper.
     *
     * @param paperId The ID of the paper to extract text from
     * @param pdfUrl The URL of the PDF file
     * @param requestedBy The user who requested the extraction
     */
    @Transactional(transactionManager = "paperTransactionManager")
    public void initiateExtraction(UUID paperId, String pdfUrl, String requestedBy) {
        try {
            log.info("Initiating text extraction for paper: {}", paperId);

            // Update paper status to indicate extraction is starting
            Paper paper = paperRepository
                    .findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + paperId));

            paper.setExtractionStatus(ExtractionStatus.IN_PROGRESS);
            paperRepository.save(paper);

            log.info("Text extraction initiated for paper: {}", paperId);

        } catch (Exception e) {
            log.error("Failed to initiate extraction for paper {}: {}", paperId, e.getMessage(), e);
            throw new RuntimeException("Failed to initiate text extraction", e);
        }
    }

    /**
     * Sanitizes text to remove null bytes and other problematic characters for PostgreSQL.
     *
     * @param text The text to sanitize
     * @return Sanitized text safe for database storage
     */
    private String sanitizeText(String text) {
        if (text == null) {
            return null;
        }

        // Remove null bytes (0x00) and other control characters except newlines and tabs
        String sanitized = text.replaceAll("[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]", "");

        // Trim excessive whitespace
        sanitized = sanitized.replaceAll("\\s+", " ").trim();

        // Limit text length if too long (optional safety measure)
        if (sanitized.length() > 1_000_000) { // 1MB limit
            log.warn("Truncating very long extracted text from {} to {} characters", sanitized.length(), 1_000_000);
            sanitized = sanitized.substring(0, 1_000_000) + "... [TRUNCATED]";
        }

        return sanitized;
    }
}
