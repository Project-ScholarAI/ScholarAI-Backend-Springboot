package dev.project.scholar_ai.model.paper.structure;

import dev.project.scholar_ai.model.paper.metadata.Paper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "human_summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HumanSummary {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false, unique = true)
    private Paper paper;

    @Column(name = "problem_motivation", columnDefinition = "TEXT")
    private String problemMotivation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "key_contributions", columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> keyContributions;

    @Column(name = "method_overview", columnDefinition = "TEXT")
    private String methodOverview;

    @Column(name = "data_experimental_setup", columnDefinition = "TEXT")
    private String dataExperimentalSetup;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headline_results", columnDefinition = "jsonb default '[]'::jsonb")
    private List<Map<String, Object>> headlineResults;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "limitations_failure_modes", columnDefinition = "jsonb default '[]'::jsonb")
    private List<String> limitationsFailureModes;

    @Column(name = "practical_implications_next_steps", columnDefinition = "TEXT")
    private String practicalImplicationsNextSteps;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods for bidirectional relationship
    public void setPaper(Paper paper) {
        this.paper = paper;
        if (paper != null && paper.getHumanSummary() != this) {
            paper.setHumanSummary(this);
        }
    }

    // Helper methods for working with data
    public boolean hasContributions() {
        return keyContributions != null && !keyContributions.isEmpty();
    }

    public boolean hasResults() {
        return headlineResults != null && !headlineResults.isEmpty();
    }

    public boolean hasLimitations() {
        return limitationsFailureModes != null && !limitationsFailureModes.isEmpty();
    }

    public int getContributionsCount() {
        return keyContributions != null ? keyContributions.size() : 0;
    }

    public int getResultsCount() {
        return headlineResults != null ? headlineResults.size() : 0;
    }
}