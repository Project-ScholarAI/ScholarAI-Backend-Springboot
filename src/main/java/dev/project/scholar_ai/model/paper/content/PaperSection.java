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
@Table(name = "paper_sections")
public class PaperSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @Column(nullable = false, length = 120)
    private String heading;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "start_page")
    private Integer startPage;

    @Column(name = "end_page")
    private Integer endPage;
} 