package dev.project.scholar_ai.service.structuring;

import dev.project.scholar_ai.dto.agent.request.StructuringRequest;
import dev.project.scholar_ai.dto.agent.request.SummarizationRequest;
import dev.project.scholar_ai.dto.agent.response.StructuringResult;
import dev.project.scholar_ai.messaging.publisher.StructuringRequestSender;
import dev.project.scholar_ai.messaging.publisher.SummarizationRequestSender;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.model.paper.structure.ExtractedDocument;
import dev.project.scholar_ai.model.paper.structure.HumanSummary;
import dev.project.scholar_ai.model.paper.structure.StructuredFacts;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import dev.project.scholar_ai.repository.paper.structure.ExtractedDocumentRepository;
import dev.project.scholar_ai.repository.paper.structure.HumanSummaryRepository;
import dev.project.scholar_ai.repository.paper.structure.StructuredFactsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StructuringService {

    private final PaperRepository paperRepository;
    private final ExtractedDocumentRepository extractedDocumentRepository;
    private final HumanSummaryRepository humanSummaryRepository;
    private final StructuredFactsRepository structuredFactsRepository;
    private final StructuringRequestSender structuringRequestSender;
    private final SummarizationRequestSender summarizationRequestSender;
    private final ObjectMapper objectMapper;

    /**
     * Triggers text structuring for a paper with extracted text.
     *
     * @param paperId The ID of the paper to structure
     * @param extractedText The extracted text content
     * @param requestedBy The user who requested the structuring
     */
    public void triggerStructuring(UUID paperId, String extractedText, String requestedBy) {
        try {
            log.info("Triggering text structuring for paper: {}", paperId);

            // Get paper metadata
            Paper paper = paperRepository.findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found: " + paperId));

            // Build paper metadata for AI processing
            Map<String, Object> paperMetadata = buildPaperMetadata(paper);

            // Create structuring request
            StructuringRequest structuringRequest = StructuringRequest.builder()
                    .correlationId(UUID.randomUUID().toString())
                    .paperId(paperId)
                    .extractedText(extractedText)
                    .paperMetadata(paperMetadata)
                    .requestedBy(requestedBy)
                    .build();

            // Send structuring request
            structuringRequestSender.send(structuringRequest);

            log.info("Text structuring request sent for paper: {}", paperId);

        } catch (Exception e) {
            log.error("Failed to trigger text structuring for paper {}: {}", paperId, e.getMessage(), e);
            // Don't throw here - extraction was successful, structuring failure shouldn't break the flow
        }
    }

    /**
     * Updates a paper with structured content from the structuring result.
     *
     * @param result The structuring result containing structured data
     */
    @Transactional(transactionManager = "paperTransactionManager")
    public void updatePaperWithStructuredContent(StructuringResult result) {
        log.info("Updating paper {} with structured content", result.getPaperId());

        try {
            Paper paper = paperRepository.findById(result.getPaperId())
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + result.getPaperId()));

            if ("COMPLETED".equals(result.getStatus())) {
                // Save extracted document with sections
                saveExtractedDocument(paper, result);

                // Save structured facts
                saveStructuredFacts(paper, result);

                // Save human summary
                saveHumanSummary(paper, result);

                log.info("Successfully saved structured content for paper {} - {} sections, {} facts fields",
                        result.getPaperId(), 
                        result.getSectionsCount(),
                        result.getStructuredFacts() != null ? result.getStructuredFacts().size() : 0);

                // Trigger summarization with structured content
                triggerSummarizationWithStructuredContent(result.getPaperId(), paper.getExtractedText());

            } else {
                log.warn("Text structuring failed for paper {}: {}", result.getPaperId(), result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("Failed to update paper {} with structured content: {}", result.getPaperId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update paper with structured content", e);
        }
    }

    /**
     * Triggers summarization for a paper that has structured content.
     */
    private void triggerSummarizationWithStructuredContent(UUID paperId, String extractedText) {
        try {
            log.info("Triggering summarization with structured content for paper: {}", paperId);

            // Create summarization request
            SummarizationRequest summarizationRequest = SummarizationRequest.builder()
                    .correlationId(UUID.randomUUID().toString())
                    .paperId(paperId)
                    .content(extractedText)
                    .requestedBy("structuring-service")
                    .build();

            // Send summarization request
            summarizationRequestSender.send(summarizationRequest);

            log.info("Summarization request sent for structured paper: {}", paperId);

        } catch (Exception e) {
            log.error("Failed to trigger summarization for structured paper {}: {}", paperId, e.getMessage(), e);
        }
    }

    private void saveExtractedDocument(Paper paper, StructuringResult result) {
        try {
            // Check if extracted document already exists
            ExtractedDocument extractedDocument = extractedDocumentRepository.findByPaperId(paper.getId())
                    .orElse(ExtractedDocument.builder()
                            .paper(paper)
                            .build());

            extractedDocument.setFullText(paper.getExtractedText());
            extractedDocument.setSections(result.getSections());

            extractedDocumentRepository.save(extractedDocument);
            log.debug("Saved extracted document with {} sections for paper {}", 
                    result.getSectionsCount(), paper.getId());

        } catch (Exception e) {
            log.error("Failed to save extracted document for paper {}: {}", paper.getId(), e.getMessage(), e);
        }
    }

    private void saveStructuredFacts(Paper paper, StructuringResult result) {
        try {
            // Check if structured facts already exist
            StructuredFacts structuredFacts = structuredFactsRepository.findByPaperId(paper.getId())
                    .orElse(StructuredFacts.builder()
                            .paper(paper)
                            .build());

            structuredFacts.setFacts(result.getStructuredFacts());

            structuredFactsRepository.save(structuredFacts);
            log.debug("Saved structured facts for paper {}", paper.getId());

        } catch (Exception e) {
            log.error("Failed to save structured facts for paper {}: {}", paper.getId(), e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void saveHumanSummary(Paper paper, StructuringResult result) {
        try {
            // Check if human summary already exists
            HumanSummary humanSummary = humanSummaryRepository.findByPaperId(paper.getId())
                    .orElse(HumanSummary.builder()
                            .paper(paper)
                            .build());

            Map<String, Object> summaryData = result.getHumanSummary();
            if (summaryData != null) {
                humanSummary.setProblemMotivation((String) summaryData.get("problem_motivation"));
                humanSummary.setKeyContributions((List<String>) summaryData.get("key_contributions"));
                humanSummary.setMethodOverview((String) summaryData.get("method_overview"));
                humanSummary.setDataExperimentalSetup((String) summaryData.get("data_experimental_setup"));
                humanSummary.setHeadlineResults((List<Map<String, Object>>) summaryData.get("headline_results"));
                humanSummary.setLimitationsFailureModes((List<String>) summaryData.get("limitations_failure_modes"));
                humanSummary.setPracticalImplicationsNextSteps((String) summaryData.get("practical_implications_next_steps"));
            }

            humanSummaryRepository.save(humanSummary);
            log.debug("Saved human summary for paper {}", paper.getId());

        } catch (Exception e) {
            log.error("Failed to save human summary for paper {}: {}", paper.getId(), e.getMessage(), e);
        }
    }

    private Map<String, Object> buildPaperMetadata(Paper paper) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", paper.getTitle());
        metadata.put("authors", paper.getAuthors() != null ? 
                paper.getAuthors().stream()
                        .map(author -> author.getName())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Unknown") : "Unknown");
        metadata.put("abstract", paper.getAbstractText());
        metadata.put("source", paper.getSource());
        metadata.put("publication_date", paper.getPublicationDate());
        metadata.put("doi", paper.getDoi());
        return metadata;
    }
}