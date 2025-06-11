package dev.project.scholar_ai.controller.library;

import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.dto.library.LibraryResponseDto;
import dev.project.scholar_ai.service.library.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/library")
@Tag(name = "Library", description = "Project library endpoints for accessing papers")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping("/project/{projectId}")
    @Operation(
            summary = "📚 Get Project Library",
            description = "Retrieve all papers for a specific project. This endpoint fetches all papers "
                    + "from completed web search operations associated with the project. "
                    + "Returns comprehensive paper metadata including titles, authors, abstracts, "
                    + "citation counts, and PDF URLs when available.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Project library retrieved successfully",
                        content = @Content(schema = @Schema(implementation = APIResponse.class))),
                @ApiResponse(responseCode = "404", description = "Project not found or no papers available")
            })
    public ResponseEntity<APIResponse<LibraryResponseDto>> getProjectLibrary(
            @PathVariable
                    @Parameter(
                            description = "Project ID to retrieve library for",
                            example = "123e4567-e89b-12d3-a456-426614174000")
                    UUID projectId) {
        try {
            log.info("Retrieving library for project: {}", projectId);

            LibraryResponseDto library = libraryService.getProjectLibrary(projectId);

            String message = library.totalPapers() > 0
                    ? String.format("Successfully retrieved %d papers from project library", library.totalPapers())
                    : "Project library is empty - no papers found";

            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), message, library));
        } catch (Exception e) {
            log.error("Error retrieving library for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve project library", null));
        }
    }

    @GetMapping("/project/{projectId}/stats")
    @Operation(
            summary = "📊 Get Project Library Statistics",
            description = "Get statistical overview of a project's library including paper count, "
                    + "search operations status, and correlation IDs summary.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Library statistics retrieved successfully",
                        content = @Content(schema = @Schema(implementation = APIResponse.class))),
                @ApiResponse(responseCode = "404", description = "Project not found")
            })
    public ResponseEntity<APIResponse<LibraryResponseDto>> getProjectLibraryStats(
            @PathVariable
                    @Parameter(
                            description = "Project ID to retrieve library statistics for",
                            example = "123e4567-e89b-12d3-a456-426614174000")
                    UUID projectId) {
        try {
            log.info("Retrieving library statistics for project: {}", projectId);

            LibraryResponseDto library = libraryService.getProjectLibrary(projectId);
            // Remove papers from response to only show statistics
            LibraryResponseDto statsOnly = new LibraryResponseDto(
                    library.projectId(),
                    library.correlationIds(),
                    library.totalPapers(),
                    library.completedSearchOperations(),
                    library.retrievedAt(),
                    library.message(),
                    List.of()); // Empty papers list for stats endpoint

            String message = String.format(
                    "Project has %d papers from %d completed search operations",
                    library.totalPapers(), library.completedSearchOperations());

            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), message, statsOnly));
        } catch (Exception e) {
            log.error("Error retrieving library statistics for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed to retrieve project library statistics",
                            null));
        }
    }
}
