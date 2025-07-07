package dev.project.scholar_ai.model.paper.structure;

import dev.project.scholar_ai.model.paper.metadata.Paper;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "extracted_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractedDocument {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false, unique = true)
    private Paper paper;

    @Column(name = "full_text", nullable = false, columnDefinition = "TEXT")
    private String fullText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sections", nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> sections;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods for bidirectional relationship
    public void setPaper(Paper paper) {
        this.paper = paper;
        if (paper != null && paper.getExtractedDocument() != this) {
            paper.setExtractedDocument(this);
        }
    }

    // Helper methods for working with sections
    public int getSectionCount() {
        return sections != null ? sections.size() : 0;
    }

    public boolean hasSections() {
        return sections != null && !sections.isEmpty();
    }
}
