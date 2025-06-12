package dev.project.scholar_ai.model.paper.content;

import dev.project.scholar_ai.model.paper.metadata.Paper;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "paper_citations")
public class PaperCitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The paper that contains this citation (citing paper)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citing_paper_id", nullable = false)
    private Paper citingPaper;

    // DOI extracted from the reference list, if available
    @Column(name = "cited_doi", length = 100)
    private String citedDoi;

    // Link to a Paper in our DB if we already have it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cited_paper_id")
    private Paper citedPaper;

    // Raw reference text for traceability
    @Column(name = "raw_citation", columnDefinition = "TEXT")
    private String rawCitation;
} 