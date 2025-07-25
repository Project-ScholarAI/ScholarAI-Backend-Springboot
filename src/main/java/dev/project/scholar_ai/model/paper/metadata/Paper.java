package dev.project.scholar_ai.model.paper.metadata;

import dev.project.scholar_ai.enums.ExtractionStatus;
import dev.project.scholar_ai.model.paper.structure.ExtractedDocument;
import dev.project.scholar_ai.model.paper.structure.HumanSummary;
import dev.project.scholar_ai.model.paper.structure.StructuredFacts;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "papers")
public class Paper {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Correlation ID reference - each paper belongs to a search operation
    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    // Core Fields
    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String abstractText;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(length = 100)
    private String doi;

    // Identifiers and Source Information
    @Column(name = "semantic_scholar_id", length = 100)
    private String semanticScholarId;

    @Column(length = 50)
    private String source;

    // PDF and Access Information
    @Column(name = "pdf_content_url", length = 500)
    private String pdfContentUrl;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "is_open_access")
    private Boolean isOpenAccess;

    @Column(name = "paper_url", length = 500)
    private String paperUrl;

    // Publication Types (stored as comma-separated values for simplicity)
    @Column(name = "publication_types", length = 200)
    private String publicationTypes;

    // Fields of Study (stored as comma-separated values for simplicity)
    @Column(name = "fields_of_study", columnDefinition = "TEXT")
    private String fieldsOfStudy;

    // Text Extraction Fields
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "extraction_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExtractionStatus extractionStatus = ExtractionStatus.PENDING;

    @Column(name = "extracted_at")
    private LocalDateTime extractedAt;

    // Relationships
    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Author> authors = new ArrayList<>();

    @OneToMany(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExternalId> externalIds = new ArrayList<>();

    @OneToOne(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PublicationVenue venue;

    @OneToOne(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PaperMetrics metrics;

    // Structured Data Relationships
    @OneToOne(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ExtractedDocument extractedDocument;

    @OneToOne(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private HumanSummary humanSummary;

    @OneToOne(mappedBy = "paper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StructuredFacts structuredFacts;

    // Helper methods
    public void addAuthor(Author author) {
        authors.add(author);
        author.setPaper(this);
    }

    public void removeAuthor(Author author) {
        authors.remove(author);
        author.setPaper(null);
    }

    public void addExternalId(ExternalId externalId) {
        externalIds.add(externalId);
        externalId.setPaper(this);
    }

    public void removeExternalId(ExternalId externalId) {
        externalIds.remove(externalId);
        externalId.setPaper(null);
    }

    // Structured Data Helper Methods
    public void setExtractedDocument(ExtractedDocument extractedDocument) {
        this.extractedDocument = extractedDocument;
        if (extractedDocument != null && extractedDocument.getPaper() != this) {
            extractedDocument.setPaper(this);
        }
    }

    public void setHumanSummary(HumanSummary humanSummary) {
        this.humanSummary = humanSummary;
        if (humanSummary != null && humanSummary.getPaper() != this) {
            humanSummary.setPaper(this);
        }
    }

    public void setStructuredFacts(StructuredFacts structuredFacts) {
        this.structuredFacts = structuredFacts;
        if (structuredFacts != null && structuredFacts.getPaper() != this) {
            structuredFacts.setPaper(this);
        }
    }

    // Status check methods
    public boolean hasExtractedDocument() {
        return extractedDocument != null;
    }

    public boolean hasHumanSummary() {
        return humanSummary != null;
    }

    public boolean hasStructuredFacts() {
        return structuredFacts != null;
    }

    public boolean isFullyStructured() {
        return hasExtractedDocument() && hasHumanSummary() && hasStructuredFacts();
    }
}
