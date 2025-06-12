package dev.project.scholar_ai.controller.summarizer;

import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.service.summarizer.SummarizerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/summarizer")
@Tag(name = "Content Extraction & Summarization", description = "Extracts structured content from PDFs and generates summaries.")
@RequiredArgsConstructor
public class SummarizerController {

    private final SummarizerService summarizerService;

    @PostMapping("/run/{correlationId}")
    @Operation(
            summary = "📄 Run Content Extraction & Summarization",
            description = "Start extraction & summarization for all papers associated with the given correlation ID.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Summarization job submitted successfully"),
                @ApiResponse(responseCode = "404", description = "No papers found for correlation ID")
            })
    public ResponseEntity<APIResponse<Map<String, String>>> runSummarization(
            @PathVariable
                    @Parameter(description = "Correlation ID from the web search operation")
                    String correlationId) {
        try {
            summarizerService.initiateSummarization(correlationId);
            return ResponseEntity.ok(APIResponse.success(
                    HttpStatus.OK.value(),
                    "Summarization job submitted successfully. Results will be available shortly.",
                    Map.of("correlationId", correlationId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error initiating summarization: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to initiate summarization", null));
        }
    }
} 