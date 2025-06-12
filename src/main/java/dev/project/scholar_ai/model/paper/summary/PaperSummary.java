package dev.project.scholar_ai.model.paper.summary;

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
@Table(name = "paper_summaries")
public class PaperSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    // Summary generation status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SummaryStatus status = SummaryStatus.PENDING;

    @Column(name = "summarized_at")
    private LocalDateTime summarizedAt;

    @Column(name = "summary_error", columnDefinition = "TEXT")
    private String summaryError;

    // Human-Friendly Summaries (as per UC-06)
    @Column(name = "problem_motivation", columnDefinition = "TEXT")
    private String problemMotivation; // What gap or pain-point does the paper address

    @Column(name = "key_contributions", columnDefinition = "TEXT")
    private String keyContributions; // Bullet list of what authors claim is new

    @Column(name = "method_overview", columnDefinition = "TEXT")
    private String methodOverview; // Core idea/architecture/training trick

    @Column(name = "data_experimental_setup", columnDefinition = "TEXT")
    private String dataExperimentalSetup; // Datasets, baselines, hardware, evaluation

    @Column(name = "headline_results", columnDefinition = "TEXT")
    private String headlineResults; // Metrics table with gains/trade-offs

    @Column(name = "limitations_failures", columnDefinition = "TEXT")
    private String limitationsFailures; // Weaknesses, scalability ceilings

    @Column(name = "practical_implications", columnDefinition = "TEXT")
    private String practicalImplications; // How practitioners might use/extend work

    // Machine-Friendly Structured Facts (JSON as per UC-06)
    @Column(name = "structured_facts", columnDefinition = "JSONB")
    private String structuredFacts; // Complete JSON object as described in UC-06

    // Additional metadata
    @Column(name = "summary_version")
    private String summaryVersion; // Track different summary versions/models

    @Column(name = "ai_model_used", length = 100)
    private String aiModelUsed; // Track which AI model generated the summary

    @Column(name = "confidence_score")
    private Double confidenceScore; // AI confidence in the summary quality

    public enum SummaryStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        OUTDATED
    }

    // Helper methods
    public void markAsInProgress() {
        this.status = SummaryStatus.IN_PROGRESS;
    }

    public void markAsCompleted(String aiModel) {
        this.status = SummaryStatus.COMPLETED;
        this.summarizedAt = LocalDateTime.now();
        this.aiModelUsed = aiModel;
        this.summaryError = null;
    }

    public void markAsFailed(String errorMessage) {
        this.status = SummaryStatus.FAILED;
        this.summarizedAt = LocalDateTime.now();
        this.summaryError = errorMessage;
    }

    public void markAsOutdated() {
        this.status = SummaryStatus.OUTDATED;
    }

    public boolean isCompleted() {
        return status == SummaryStatus.COMPLETED;
    }

    public boolean hasFailed() {
        return status == SummaryStatus.FAILED;
    }

    public boolean needsUpdate() {
        return status == SummaryStatus.PENDING || status == SummaryStatus.OUTDATED;
    }
} 