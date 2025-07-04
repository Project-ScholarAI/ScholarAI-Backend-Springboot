package dev.project.scholar_ai.controller;

import dev.project.scholar_ai.dto.agent.request.ExtractionRequest;
import dev.project.scholar_ai.dto.agent.response.ExtractionResult;
import dev.project.scholar_ai.messaging.publisher.ExtractionRequestSender;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import dev.project.scholar_ai.service.extraction.ExtractionService;
import dev.project.scholar_ai.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/test/extraction")
@RequiredArgsConstructor
public class ExtractionTestController {

    private final PaperRepository paperRepository;
    private final ExtractionRequestSender extractionRequestSender;
    private final ExtractionService extractionService;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;

    /**
     * Trigger text extraction for a specific paper
     */
    @PostMapping("/papers/{paperId}/extract")
    public ResponseEntity<Map<String, Object>> triggerExtraction(
            @PathVariable UUID paperId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Manual extraction triggered for paper: {}", paperId);
            
            // Find the paper
            Paper paper = paperRepository.findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + paperId));
            
            // Check if paper has PDF URL
            if (paper.getPdfUrl() == null || paper.getPdfUrl().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Paper has no PDF URL");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create extraction request
            ExtractionRequest request = ExtractionRequest.builder()
                    .correlationId(UUID.randomUUID().toString())
                    .paperId(paperId)
                    .pdfUrl(paper.getPdfUrl())
                    .requestedBy(authentication != null ? authentication.getName() : "test-user")
                    .build();
            
            // Update paper status to IN_PROGRESS
            extractionService.initiateExtraction(paperId, paper.getPdfUrl(), request.getRequestedBy());
            
            // Send extraction request
            extractionRequestSender.send(request);
            
            response.put("success", true);
            response.put("message", "Extraction request sent successfully");
            response.put("paperId", paperId);
            response.put("correlationId", request.getCorrelationId());
            response.put("pdfUrl", paper.getPdfUrl());
            
            log.info("Extraction request sent for paper: {} with correlation ID: {}", 
                    paperId, request.getCorrelationId());
            
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
    @GetMapping("/papers/{paperId}/status")
    public ResponseEntity<Map<String, Object>> getExtractionStatus(@PathVariable UUID paperId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Paper paper = paperRepository.findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found with ID: " + paperId));
            
            response.put("success", true);
            response.put("paperId", paperId);
            response.put("extractionStatus", paper.getExtractionStatus());
            response.put("extractedAt", paper.getExtractedAt());
            response.put("hasExtractedText", paper.getExtractedText() != null);
            response.put("textLength", paper.getExtractedText() != null ? paper.getExtractedText().length() : 0);
            response.put("pdfUrl", paper.getPdfUrl());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get extraction status for paper {}: {}", paperId, e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get papers that are ready for extraction (have PDF URLs but no extracted text)
     */
    @GetMapping("/papers/ready-for-extraction")
    public ResponseEntity<Map<String, Object>> getPapersReadyForExtraction(
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Find papers with PDF URLs but no extracted text
            var papers = paperRepository.findByPdfUrlIsNotNullAndExtractedTextIsNull()
                    .stream()
                    .limit(limit)
                    .map(paper -> {
                        Map<String, Object> paperInfo = new HashMap<>();
                        paperInfo.put("id", paper.getId());
                        paperInfo.put("title", paper.getTitle());
                        paperInfo.put("pdfUrl", paper.getPdfUrl());
                        paperInfo.put("extractionStatus", paper.getExtractionStatus());
                        paperInfo.put("source", paper.getSource());
                        return paperInfo;
                    })
                    .toList();
            
            response.put("success", true);
            response.put("papers", papers);
            response.put("count", papers.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get papers ready for extraction: {}", e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Trigger extraction for multiple papers with PDF URLs
     */
    @PostMapping("/papers/extract-batch")
    public ResponseEntity<Map<String, Object>> triggerBatchExtraction(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Find papers ready for extraction
            var papers = paperRepository.findByPdfUrlIsNotNullAndExtractedTextIsNull()
                    .stream()
                    .limit(limit)
                    .toList();
            
            if (papers.isEmpty()) {
                response.put("success", true);
                response.put("message", "No papers found that are ready for extraction");
                response.put("processedCount", 0);
                return ResponseEntity.ok(response);
            }
            
            int processedCount = 0;
            String requestedBy = authentication != null ? authentication.getName() : "test-user";
            
            for (Paper paper : papers) {
                try {
                    ExtractionRequest request = ExtractionRequest.builder()
                            .correlationId(UUID.randomUUID().toString())
                            .paperId(paper.getId())
                            .pdfUrl(paper.getPdfUrl())
                            .requestedBy(requestedBy)
                            .build();
                    
                    // Update paper status
                    extractionService.initiateExtraction(paper.getId(), paper.getPdfUrl(), requestedBy);
                    
                    // Send extraction request
                    extractionRequestSender.send(request);
                    
                    processedCount++;
                    
                    // Small delay to avoid overwhelming the system
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    log.error("Failed to process extraction for paper {}: {}", paper.getId(), e.getMessage());
                }
            }
            
            response.put("success", true);
            response.put("message", "Batch extraction requests sent");
            response.put("totalPapers", papers.size());
            response.put("processedCount", processedCount);
            
            log.info("Batch extraction triggered for {} papers", processedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to trigger batch extraction: {}", e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/papers/{paperId}/simulate-extraction-result")
    public ResponseEntity<Map<String, Object>> simulateExtractionResult(
            @PathVariable UUID paperId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Create a fake extraction result to test database update
            ExtractionResult fakeResult = ExtractionResult.builder()
                    .correlationId(UUID.randomUUID().toString())
                    .paperId(paperId)
                    .extractedText("This is fake extracted text for testing purposes. The quick brown fox jumps over the lazy dog.")
                    .status("COMPLETED")
                    .errorMessage(null)
                    .extractionMethod("TEST")
                    .textLength(95)
                    .build();
            
            // Call the extraction service directly
            extractionService.updatePaperWithExtractedText(fakeResult);
            
            response.put("success", true);
            response.put("message", "Successfully simulated extraction result");
            response.put("paperId", paperId);
            response.put("textLength", 95);
            
            log.info("Simulated extraction result for paper {}", paperId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to simulate extraction result for paper {}: {}", paperId, e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/papers/{paperId}/test-rabbitmq")
    public ResponseEntity<Map<String, Object>> testRabbitMQDirect(
            @PathVariable UUID paperId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Create a test extraction result and send it directly to the listener
            ExtractionResult testResult = ExtractionResult.builder()
                    .correlationId(UUID.randomUUID().toString())
                    .paperId(paperId)
                    .extractedText("TEST MESSAGE: This is a direct RabbitMQ test message with real extracted text from FastAPI.")
                    .status("COMPLETED")
                    .errorMessage(null)
                    .extractionMethod("RABBITMQ_TEST")
                    .textLength(95)
                    .build();
            
            // Send directly to RabbitMQ completed queue (this tests if Spring Boot can send/receive)
            rabbitTemplate.convertAndSend(
                rabbitMQConfig.getExchangeName(), 
                rabbitMQConfig.getExtractionCompletedRoutingKey(), 
                testResult
            );
            
            response.put("success", true);
            response.put("message", "Test message sent to RabbitMQ queue");
            response.put("paperId", paperId);
            response.put("correlationId", testResult.getCorrelationId());
            
            log.info("Sent test RabbitMQ message for paper {}", paperId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to send test RabbitMQ message for paper {}: {}", paperId, e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}