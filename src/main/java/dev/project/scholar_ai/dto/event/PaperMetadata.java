package dev.project.scholar_ai.dto.event;

import java.util.List;

public record PaperMetadata(String doi, String title, List<String> authors, String pdfUrl) {}
