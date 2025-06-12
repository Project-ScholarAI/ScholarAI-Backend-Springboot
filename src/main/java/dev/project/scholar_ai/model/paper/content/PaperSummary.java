package dev.project.scholar_ai.model.paper.content;

import dev.project.scholar_ai.model.paper.metadata.Paper;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    @JoinColumn(name = "paper_id", nullable = false, unique = true)
    private Paper paper;

    /**
     * JSON string that stores the machine-friendly structured summary as defined
     * in UC-06. Uses JSONB column type when supported (e.g. PostgreSQL).
     */
    @Column(columnDefinition = "JSONB")
    private String summaryJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 