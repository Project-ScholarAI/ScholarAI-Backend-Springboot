package dev.project.scholar_ai.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Detailed information about an academic paper")
public record PaperDetailsDto(
        @Schema(description = "Paper title", example = "Deep Learning for Natural Language Processing") String title,
        @Schema(description = "Digital Object Identifier", example = "10.1000/182") String doi,
        @Schema(description = "Publication date", example = "2023-05-15") LocalDate publicationDate,
        @Schema(description = "Venue/Journal name", example = "Nature Machine Intelligence") String venueName,
        @Schema(description = "Publisher", example = "Nature Publishing Group") String publisher,
        @Schema(description = "Whether the paper is peer-reviewed", example = "true") Boolean peerReviewed,
        @Schema(description = "List of authors") List<AuthorDetailsDto> authors,
        @Schema(description = "Number of citations", example = "142") Integer citationCount,
        @Schema(description = "URL to code repository", example = "https://github.com/author/repo")
                String codeRepositoryUrl,
        @Schema(description = "URL to dataset", example = "https://dataset.example.com") String datasetUrl,
        @Schema(description = "URL to paper", example = "https://arxiv.org/abs/2301.12345") String paperUrl,
        @Schema(description = "Direct PDF download URL", example = "https://arxiv.org/pdf/2301.12345.pdf")
                String pdfUrl,
        @Schema(description = "Source where paper was found", example = "Semantic Scholar") String source,
        @Schema(description = "Abstract or summary", example = "This paper presents a novel approach...")
                String abstractText) {}
