package dev.project.scholar_ai.controller.summarizer;

import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.dto.summarizer.PaperContentRequest;
import dev.project.scholar_ai.service.summarizer.PaperContentPersistenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/summarizer/content")
@Tag(name = "Summarizer Content", description = "Persist extracted sections, citations, summaries from FastAPI agent")
@RequiredArgsConstructor
public class SummarizerContentController {

    private final PaperContentPersistenceService persistenceService;

    @PostMapping
    @Operation(summary = "Persist extracted content for a paper")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Content persisted")})
    public ResponseEntity<APIResponse<Void>> persistContent(@RequestBody PaperContentRequest request) {
        try {
            persistenceService.persistContent(request);
            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Content saved", null));
        } catch (Exception e) {
            log.error("Error persisting content: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }
} 