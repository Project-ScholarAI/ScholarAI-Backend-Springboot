package dev.project.scholar_ai.dto.event;

import java.time.LocalDate;
import java.util.List;

public record EnhancedPaperMetadata(
    String title,
    String doi,
    LocalDate publicationDate,
    String venueName,
    String publisher,
    Boolean peerReviewed,
    List<AuthorInfo> authors,
    Integer citationCount,
    String codeRepositoryUrl,
    String datasetUrl,
    String paperUrl,
    String pdfUrl, // Direct PDF download URL
    String pdfContent // base64 encoded PDF content if downloaded
) {} 