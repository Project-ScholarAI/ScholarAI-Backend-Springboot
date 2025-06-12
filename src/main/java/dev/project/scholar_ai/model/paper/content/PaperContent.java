package dev.project.scholar_ai.model.paper.content;

import dev.project.scholar_ai.model.paper.metadata.Paper;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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
@Table(name = "paper_content")
public class PaperContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    // Content extraction status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ExtractionStatus status = ExtractionStatus.PENDING;

    @Column(name = "extracted_at")
    private LocalDateTime extractedAt;

    @Column(name = "extraction_error", columnDefinition = "TEXT")
    private String extractionError;

    // Raw extracted text
    @Column(name = "full_text", columnDefinition = "TEXT")
    private String fullText;

    // Structured sections
    @Column(name = "title_extracted", columnDefinition = "TEXT")
    private String titleExtracted;

    @Column(name = "abstract_extracted", columnDefinition = "TEXT")
    private String abstractExtracted;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "methodology", columnDefinition = "TEXT")
    private String methodology;

    @Column(name = "results", columnDefinition = "TEXT")
    private String results;

    @Column(name = "discussion", columnDefinition = "TEXT")
    private String discussion;

    @Column(name = "conclusion", columnDefinition = "TEXT")
    private String conclusion;

    @Column(name = "references", columnDefinition = "TEXT")
    private String references;

    @Column(name = "acknowledgments", columnDefinition = "TEXT")
    private String acknowledgments;

    // Keywords and key phrases extracted from content
    @Column(name = "extracted_keywords", columnDefinition = "TEXT")
    private String extractedKeywords; // JSON array

    @Column(name = "key_phrases", columnDefinition = "TEXT")
    private String keyPhrases; // JSON array

    // Figures and tables metadata
    @Column(name = "figures_count")
    private Integer figuresCount;

    @Column(name = "tables_count")
    private Integer tablesCount;

    @Column(name = "equations_count")
    private Integer equationsCount;

    // Content statistics
    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "section_count")
    private Integer sectionCount;

    @Column(name = "reference_count")
    private Integer referenceCount;

    public enum ExtractionStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        SKIPPED
    }

    // Helper methods
    public void markAsInProgress() {
        this.status = ExtractionStatus.IN_PROGRESS;
    }

    public void markAsCompleted() {
        this.status = ExtractionStatus.COMPLETED;
        this.extractedAt = LocalDateTime.now();
        this.extractionError = null;
    }

    public void markAsFailed(String errorMessage) {
        this.status = ExtractionStatus.FAILED;
        this.extractedAt = LocalDateTime.now();
        this.extractionError = errorMessage;
    }

    public boolean isCompleted() {
        return status == ExtractionStatus.COMPLETED;
    }

    public boolean hasFailed() {
        return status == ExtractionStatus.FAILED;
    }
} 