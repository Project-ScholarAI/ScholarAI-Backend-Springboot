package dev.project.scholar_ai.controller;

import dev.project.scholar_ai.dto.agentRequests.SummarizationRequest;
import dev.project.scholar_ai.messaging.publisher.SummarizationRequestSender;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final SummarizationRequestSender summarizationRequestSender;

    public DemoController(SummarizationRequestSender summarizationRequestSender) {
        this.summarizationRequestSender = summarizationRequestSender;
    }

    @PostMapping("/trigger-summarization")
    public ResponseEntity<Map<String, Object>> triggerSummarization(@RequestBody Map<String, String> request) {
        String pdfUrl = request.getOrDefault("pdfUrl", "https://example.com/sample.pdf");
        UUID paperId = UUID.randomUUID();
        String correlationId = UUID.randomUUID().toString();

        // Create and send the summarization request
        SummarizationRequest summarizationRequest = new SummarizationRequest(paperId, pdfUrl, correlationId);

        summarizationRequestSender.send(summarizationRequest);

        return ResponseEntity.ok(Map.of(
                "message",
                "Summarization job submitted successfully",
                "paperId",
                paperId.toString(),
                "correlationId",
                correlationId,
                "pdfUrl",
                pdfUrl,
                "status",
                "SUBMITTED"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ScholarAI Demo Controller"));
    }
}
