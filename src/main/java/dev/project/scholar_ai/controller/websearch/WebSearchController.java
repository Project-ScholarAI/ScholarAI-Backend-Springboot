package dev.project.scholar_ai.controller.websearch;

import dev.project.scholar_ai.dto.agent.request.WebSearchRequestDTO;
import dev.project.scholar_ai.dto.agent.response.WebSearchResponseDto;
import dev.project.scholar_ai.service.WebSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/websearch")
@Tag(name = "ScholarAI Demo", description = "Demo endpoints for testing ScholarAI functionality")
public class WebSearchController {

    private final WebSearchService webSearchService;

    public WebSearchController(WebSearchService webSearchService) {
        this.webSearchService = webSearchService;
    }

    @PostMapping
    @Operation(
            summary = "🔍 Search Academic Papers",
            description =
                    "Search for academic papers across multiple sources (Semantic Scholar, arXiv, Crossref, PubMed). "
                            + "Specify your search terms, academic domain, and how many papers you want to find.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Web search initiated successfully",
                        content = @Content(schema = @Schema(implementation = WebSearchResponseDto.class))),
                @ApiResponse(responseCode = "400", description = "Invalid request parameters")
            })
    public ResponseEntity<WebSearchResponseDto> searchPapers(
            @Valid
                    @RequestBody
                    @Parameter(description = "Search parameters including query terms, domain, and batch size")
                    WebSearchRequestDTO request) {

        WebSearchResponseDto response = webSearchService.initiateWebSearch(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{correlationId}")
    @Operation(
            summary = "📄 Get Search Results",
            description = "Retrieve the results of a web search using the correlation ID. "
                    + "If the search is still in progress, papers list will be empty.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Search results retrieved successfully",
                        content = @Content(schema = @Schema(implementation = WebSearchResponseDto.class))),
                @ApiResponse(responseCode = "404", description = "Search not found")
            })
    public ResponseEntity<WebSearchResponseDto> getSearchResults(
            @PathVariable
                    @Parameter(description = "Correlation ID from the initial search request", example = "corr-123-456")
                    String correlationId) {

        WebSearchResponseDto result = webSearchService.getSearchResults(correlationId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping
    @Operation(
            summary = "📚 Get All Search Results",
            description =
                    "Retrieve all web search results. Useful for seeing the history of searches and their current status.")
    @ApiResponse(responseCode = "200", description = "All search results retrieved successfully")
    public ResponseEntity<List<WebSearchResponseDto>> getAllSearchResults() {
        List<WebSearchResponseDto> results = webSearchService.getAllSearchResults();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Check if the service is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ScholarAI Demo Controller",
                "features", "WebSearch"));
    }
}
