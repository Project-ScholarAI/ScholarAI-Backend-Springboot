package dev.project.scholar_ai.controller.paper;

import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.dto.paper.metadata.PaperPdfDto;
import dev.project.scholar_ai.service.paper.PaperQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
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
} 