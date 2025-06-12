package dev.project.scholar_ai.model.paper.citation;

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
@Table(name = "citations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"citing_paper_id", "cited_paper_id"}))
public class Citation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The paper that is doing the citing
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citing_paper_id", nullable = false)
    private Paper citingPaper;

    // The paper being cited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cited_paper_id", nullable = false)
    private Paper citedPaper;

    // Citation metadata
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CitationType citationType = CitationType.REFERENCE;

    @Column(name = "citation_context", columnDefinition = "TEXT")
    private String citationContext; // Text context where citation appears

    @Column(name = "citation_purpose", length = 100)
    private String citationPurpose; // Why this paper was cited (background, comparison, etc.)

    @Column(name = "section_mentioned")
    private String sectionMentioned; // Which section contains the citation

    @Column(name = "is_self_citation")
    private Boolean isSelfCitation; // Same authors

    @Column(name = "citation_strength")
    private Double citationStrength; // How important/relevant the citation is (0.0-1.0)

    // External citation identifiers (if available)
    @Column(name = "external_citation_id", length = 200)
    private String externalCitationId;

    @Column(name = "source_database", length = 50)
    private String sourceDatabase; // Where we found this citation

    // Temporal information
    @Column(name = "discovered_at", nullable = false)
    @Builder.Default
    private LocalDateTime discoveredAt = LocalDateTime.now();

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    public enum CitationType {
        REFERENCE,      // Standard reference/citation
        SELF_CITATION,  // Author citing their own work
        CO_CITATION,    // Papers that are frequently cited together
        BIBLIOGRAPHIC_COUPLING, // Papers that cite the same sources
        INFLUENTIAL     // Highly influential citation
    }

    // Helper methods
    public void verify() {
        this.verifiedAt = LocalDateTime.now();
    }

    public boolean isVerified() {
        return verifiedAt != null;
    }

    public void updateCitationStrength(double strength) {
        this.citationStrength = Math.max(0.0, Math.min(1.0, strength));
    }
} 