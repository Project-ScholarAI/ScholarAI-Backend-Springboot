package dev.project.scholar_ai.service.extraction;

import dev.project.scholar_ai.dto.agent.response.ExtractionResult;
import dev.project.scholar_ai.dto.agent.request.SummarizationRequest;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.enums.ExtractionStatus;
import dev.project.scholar_ai.messaging.publisher.SummarizationRequestSender;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractionService {

    private final PaperRepository paperRepository;
    private final SummarizationRequestSender summarizationRequestSender;

    /**
     * Updates a paper with extracted text content from the extraction result.
     *
     * @param result The extraction result containing paper ID and extracted text
     */
    @Transactional(transactionManager = "paperTransactionManager")
    public void updatePaperWithExtractedText(ExtractionResult result) {
        log.info("Updating paper {} with extraction results", result.getPaperId());
        
        try {
            Paper paper = paperRepository.findById(result.getPaperId())
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + result.getPaperId()));

            // Update paper with extracted text
            paper.setExtractedText(result.getExtractedText());
            paper.setExtractionStatus(ExtractionStatus.valueOf(result.getStatus()));
            paper.setExtractedAt(LocalDateTime.now());

            // Save the updated paper
            paperRepository.save(paper);
            
            log.info("Successfully updated paper {} with extracted text (length: {} characters)", 
                    result.getPaperId(), 
                    result.getTextLength());
                    
        } catch (Exception e) {
            log.error("Failed to update paper {} with extracted text: {}", 
                    result.getPaperId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update paper with extracted text", e);
        }
    }

    /**
     * Triggers summarization for a paper with extracted text.
     *
     * @param paperId The ID of the paper to summarize
     * @param extractedText The extracted text content
     */
    public void triggerSummarization(UUID paperId, String extractedText) {
        try {
            log.info("Triggering summarization for paper: {}", paperId);
            
            // Create summarization request
            SummarizationRequest summarizationRequest = SummarizationRequest.builder()
                    .correlationId(UUID.randomUUID().toString())
                    .paperId(paperId)
                    .content(extractedText)
                    .requestedBy("extraction-service")
                    .build();

            // Send summarization request
            summarizationRequestSender.send(summarizationRequest);
            
            log.info("Summarization request sent for paper: {}", paperId);
            
        } catch (Exception e) {
            log.error("Failed to trigger summarization for paper {}: {}", 
                    paperId, e.getMessage(), e);
            // Don't throw here - extraction was successful, summarization failure shouldn't break the flow
        }
    }

    /**
     * Gets the extraction status for a paper.
     *
     * @param paperId The ID of the paper
     * @return The extraction status
     */
    public ExtractionStatus getExtractionStatus(UUID paperId) {
        return paperRepository.findById(paperId)
                .map(Paper::getExtractionStatus)
                .orElse(ExtractionStatus.PENDING);
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
            Paper paper = paperRepository.findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + paperId));
            
            paper.setExtractionStatus(ExtractionStatus.IN_PROGRESS);
            paperRepository.save(paper);
            
            log.info("Text extraction initiated for paper: {}", paperId);
            
        } catch (Exception e) {
            log.error("Failed to initiate extraction for paper {}: {}", 
                    paperId, e.getMessage(), e);
            throw new RuntimeException("Failed to initiate text extraction", e);
        }
    }
}