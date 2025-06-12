package dev.project.scholar_ai.controller.paper;

import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.dto.summarizer.PaperCitationDto;
import dev.project.scholar_ai.dto.summarizer.PaperSectionDto;
import dev.project.scholar_ai.dto.summarizer.PaperSummaryDto;
import dev.project.scholar_ai.service.summarizer.PaperContentReadService;
import dev.project.scholar_ai.dto.paper.metadata.PaperPdfDto;
import dev.project.scholar_ai.service.paper.PaperQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/papers")
@Tag(name = "Papers", description = "Endpoints to retrieve paper information")
@RequiredArgsConstructor
public class PaperController {

    private final PaperQueryService paperQueryService;
    private final PaperContentReadService contentReadService;

    @GetMapping("/{paperId}/pdf")
    @Operation(summary = "Get PDF URLs for a paper")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success"), @ApiResponse(responseCode = "404", description = "Paper not found")})
    public ResponseEntity<APIResponse<PaperPdfDto>> getPdfInfo(
            @PathVariable @Parameter(description = "Paper UUID") UUID paperId) {
        try {
            PaperPdfDto dto = paperQueryService.getPdfInfo(paperId);
            return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "PDF info retrieved", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(APIResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to retrieve info", null));
        }
    }

    @GetMapping("/{paperId}/sections")
    @Operation(summary = "Get extracted sections for a paper")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Paper or sections not found")
    })
    public ResponseEntity<APIResponse<List<PaperSectionDto>>> getSections(@PathVariable UUID paperId) {
        List<PaperSectionDto> sections = contentReadService.getSections(paperId);
        return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Sections retrieved", sections));
    }

    @GetMapping("/{paperId}/citations")
    @Operation(summary = "Get extracted citations for a paper")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Paper or citations not found")
    })
    public ResponseEntity<APIResponse<List<PaperCitationDto>>> getCitations(@PathVariable UUID paperId) {
        List<PaperCitationDto> citations = contentReadService.getCitations(paperId);
        return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Citations retrieved", citations));
    }

    @GetMapping("/{paperId}/summary")
    @Operation(summary = "Get structured summary JSON for a paper")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Paper or summary not found")
    })
    public ResponseEntity<APIResponse<PaperSummaryDto>> getSummary(@PathVariable UUID paperId) {
        String summaryJson = contentReadService.getSummaryJson(paperId);
        PaperSummaryDto dto = new PaperSummaryDto(paperId, summaryJson);
        return ResponseEntity.ok(APIResponse.success(HttpStatus.OK.value(), "Summary retrieved", dto));
    }
} 