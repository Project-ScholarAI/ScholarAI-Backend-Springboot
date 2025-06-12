package dev.project.scholar_ai.dto.paper.metadata;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "PDF URLs for a paper")
public record PaperPdfDto(
        @Schema(description = "Paper ID") UUID id,
        @Schema(description = "Stored PDF content URL (Backblaze / S3)") String pdfContentUrl,
        @Schema(description = "Original PDF URL from source") String pdfUrl) {} 