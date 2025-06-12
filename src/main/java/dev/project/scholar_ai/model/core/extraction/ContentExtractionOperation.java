package dev.project.scholar_ai.model.core.extraction;

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
@Table(name = "content_extraction_operations")
public class ContentExtractionOperation {

    @Id
    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ExtractionStatus status = ExtractionStatus.SUBMITTED;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Source correlation ID from the websearch operation
    @Column(name = "source_correlation_id", nullable = false, length = 100)
    private String sourceCorrelationId;

    @Column(name = "total_papers_to_process")
    private Integer totalPapersToProcess;

    @Column(name = "papers_processed")
    private Integer papersProcessed;

    @Column(name = "papers_extracted")
    private Integer papersExtracted;

    @Column(name = "papers_summarized")
    private Integer papersSummarized;

    @Column(name = "citations_extracted")
    private Integer citationsExtracted;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "processing_duration_ms")
    private Long processingDurationMs;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum ExtractionStatus {
        SUBMITTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED,
        PARTIALLY_COMPLETED
    }

    // Helper methods
    public void markAsInProgress() {
        this.status = ExtractionStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsCompleted(int processed, int extracted, int summarized, int citations) {
        this.status = ExtractionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.papersProcessed = processed;
        this.papersExtracted = extracted;
        this.papersSummarized = summarized;
        this.citationsExtracted = citations;

        if (this.submittedAt != null && this.completedAt != null) {
            this.processingDurationMs = java.time.Duration.between(this.submittedAt, this.completedAt)
                    .toMillis();
        }
    }

    public void markAsPartiallyCompleted(int processed, int extracted, int summarized, int citations, String error) {
        this.status = ExtractionStatus.PARTIALLY_COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.papersProcessed = processed;
        this.papersExtracted = extracted;
        this.papersSummarized = summarized;
        this.citationsExtracted = citations;
        this.errorMessage = error;

        if (this.submittedAt != null && this.completedAt != null) {
            this.processingDurationMs = java.time.Duration.between(this.submittedAt, this.completedAt)
                    .toMillis();
        }
    }

    public void markAsFailed(String errorMessage) {
        this.status = ExtractionStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;

        if (this.submittedAt != null && this.completedAt != null) {
            this.processingDurationMs = java.time.Duration.between(this.submittedAt, this.completedAt)
                    .toMillis();
        }
    }

    public void updateProgress(int processed, int extracted, int summarized, int citations) {
        this.papersProcessed = processed;
        this.papersExtracted = extracted;
        this.papersSummarized = summarized;
        this.citationsExtracted = citations;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return status == ExtractionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == ExtractionStatus.FAILED;
    }

    public boolean isInProgress() {
        return status == ExtractionStatus.IN_PROGRESS || status == ExtractionStatus.SUBMITTED;
    }

    public double getCompletionPercentage() {
        if (totalPapersToProcess == null || totalPapersToProcess == 0) {
            return 0.0;
        }
        return (double) (papersProcessed != null ? papersProcessed : 0) / totalPapersToProcess * 100.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 