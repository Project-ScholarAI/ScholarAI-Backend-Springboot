package dev.project.scholar_ai.controller;

import dev.project.scholar_ai.dto.agent.request.ExtractionRequest;
import dev.project.scholar_ai.messaging.publisher.ExtractionRequestSender;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.model.paper.structure.StructuredFacts;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import dev.project.scholar_ai.repository.paper.structure.StructuredFactsRepository;
import dev.project.scholar_ai.service.extraction.ExtractionService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
public class PaperController {

    private final PaperRepository paperRepository;
    private final StructuredFactsRepository structuredFactsRepository;
    private final ExtractionRequestSender extractionRequestSender;
    private final ExtractionService extractionService;

    /**
     * Get all papers with pagination and sorting
     */
    @GetMapping
    public ResponseEntity<Page<Paper>> getAllPapers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Paper> papers = paperRepository.findAll(pageable);

        return ResponseEntity.ok(papers);
    }

    /**
     * Get a specific paper by ID
     */
    @GetMapping("/{paperId}")
    public ResponseEntity<Paper> getPaper(@PathVariable UUID paperId) {
        Paper paper = paperRepository
                .findById(paperId)
                .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + paperId));

        return ResponseEntity.ok(paper);
    }

    /**
     * Trigger text extraction for a specific paper
     */
    @PostMapping("/{paperId}/extract")
    public ResponseEntity<Map<String, Object>> triggerExtraction(
            @PathVariable UUID paperId, Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Extraction triggered for paper: {}", paperId);

            // Find the paper
            Paper paper = paperRepository
                    .findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + paperId));

            // Check if paper has PDF URL (prefer B2 storage URL)
            String pdfUrl = paper.getPdfContentUrl() != null ? paper.getPdfContentUrl() : paper.getPdfUrl();

            if (pdfUrl == null || pdfUrl.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Paper has no PDF URL available");
                return ResponseEntity.badRequest().body(response);
            }

            // Create extraction request
            ExtractionRequest request = ExtractionRequest.builder()
                    .correlationId(UUID.randomUUID().toString())
                    .paperId(paperId)
                    .pdfUrl(pdfUrl)
                    .requestedBy(authentication != null ? authentication.getName() : "system")
                    .build();

            // Update paper status to IN_PROGRESS
            extractionService.initiateExtraction(paperId, pdfUrl, request.getRequestedBy());

            // Send extraction request
            extractionRequestSender.send(request);

            response.put("success", true);
            response.put("message", "Text extraction started successfully");
            response.put("paperId", paperId);
            response.put("correlationId", request.getCorrelationId());

            log.info(
                    "Extraction request sent for paper: {} with correlation ID: {}",
                    paperId,
                    request.getCorrelationId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to trigger extraction for paper {}: {}", paperId, e.getMessage(), e);

            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get extraction status for a paper
     */
    @GetMapping("/{paperId}/extraction-status")
    public ResponseEntity<Map<String, Object>> getExtractionStatus(@PathVariable UUID paperId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Paper paper = paperRepository
                    .findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + paperId));

            response.put("success", true);
            response.put("paperId", paperId);
            response.put("extractionStatus", paper.getExtractionStatus());
            response.put("extractedAt", paper.getExtractedAt());
            response.put("hasExtractedText", paper.getExtractedText() != null);
            response.put(
                    "textLength",
                    paper.getExtractedText() != null ? paper.getExtractedText().length() : 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get extraction status for paper {}: {}", paperId, e.getMessage());

            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get extracted text for a paper
     */
    @GetMapping("/{paperId}/extracted-text")
    public ResponseEntity<Map<String, Object>> getExtractedText(@PathVariable UUID paperId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Paper paper = paperRepository
                    .findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + paperId));

            if (paper.getExtractedText() == null) {
                response.put("success", false);
                response.put("error", "No extracted text available for this paper");
                return ResponseEntity.notFound().build();
            }

            response.put("success", true);
            response.put("paperId", paperId);
            response.put("extractedText", paper.getExtractedText());
            response.put("extractedAt", paper.getExtractedAt());
            response.put("extractionStatus", paper.getExtractionStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get extracted text for paper {}: {}", paperId, e.getMessage());

            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Search papers by title or content
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Paper>> searchPapers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        Page<Paper> papers = paperRepository.findByTitleContainingIgnoreCaseOrAbstractTextContainingIgnoreCase(
                query, query, pageable);

        return ResponseEntity.ok(papers);
    }

    /**
     * Get papers by extraction status
     */
    @GetMapping("/by-extraction-status")
    public ResponseEntity<Page<Paper>> getPapersByExtractionStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        Page<Paper> papers = paperRepository.findByExtractionStatus(
                dev.project.scholar_ai.enums.ExtractionStatus.valueOf(status.toUpperCase()), pageable);

        return ResponseEntity.ok(papers);
    }

    /**
     * Get structured facts for a paper
     */
    @GetMapping("/{paperId}/structured-facts")
    public ResponseEntity<Map<String, Object>> getStructuredFacts(@PathVariable UUID paperId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Check if paper exists
            Paper paper = paperRepository
                    .findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + paperId));

            // Get structured facts
            StructuredFacts structuredFacts = structuredFactsRepository
                    .findByPaperId(paperId)
                    .orElse(null);

            response.put("success", true);
            response.put("paperId", paperId);
            response.put("hasStructuredFacts", structuredFacts != null);
            
            if (structuredFacts != null) {
                // Create a clean response object without circular references
                Map<String, Object> factsResponse = new HashMap<>();
                factsResponse.put("id", structuredFacts.getId());
                factsResponse.put("paperId", paperId);
                factsResponse.put("facts", structuredFacts.getFacts());
                factsResponse.put("createdAt", structuredFacts.getCreatedAt());
                factsResponse.put("updatedAt", structuredFacts.getUpdatedAt());
                
                response.put("structuredFacts", factsResponse);
                response.put("structuredAt", structuredFacts.getCreatedAt());
            } else {
                response.put("message", "No structured facts found for this paper");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get structured facts for paper {}: {}", paperId, e.getMessage());

            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Check if paper has structured data
     */
    @GetMapping("/{paperId}/has-structured-data")
    public ResponseEntity<Map<String, Object>> hasStructuredData(@PathVariable UUID paperId) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean hasStructuredFacts = structuredFactsRepository.existsByPaperId(paperId);

            response.put("success", true);
            response.put("paperId", paperId);
            response.put("hasStructuredFacts", hasStructuredFacts);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to check structured data for paper {}: {}", paperId, e.getMessage());

            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Manual trigger for structuring (for testing)
     */
    @PostMapping("/{paperId}/trigger-structuring")
    public ResponseEntity<Map<String, Object>> triggerStructuring(@PathVariable UUID paperId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Paper paper = paperRepository
                    .findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + paperId));

            if (paper.getExtractedText() == null || paper.getExtractedText().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Paper has no extracted text. Run extraction first.");
                return ResponseEntity.badRequest().body(response);
            }

            // Manually trigger structuring
            extractionService.triggerStructuring(paperId, paper.getExtractedText());

            response.put("success", true);
            response.put("message", "Structuring triggered manually");
            response.put("paperId", paperId);
            response.put("textLength", paper.getExtractedText().length());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to trigger structuring for paper {}: {}", paperId, e.getMessage());

            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
