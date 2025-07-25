package dev.project.scholar_ai.model.paper.structure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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
@Table(name = "structured_facts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StructuredFacts {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false, unique = true)
    @JsonIgnore
    private Paper paper;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "facts", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> facts;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods for bidirectional relationship
    public void setPaper(Paper paper) {
        this.paper = paper;
        if (paper != null && paper.getStructuredFacts() != this) {
            paper.setStructuredFacts(this);
        }
    }

    // Helper methods for working with facts
    public boolean hasFacts() {
        return facts != null && !facts.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAuthors() {
        if (facts == null) return null;
        return (Map<String, Object>) facts.get("authors");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMethodology() {
        if (facts == null) return null;
        return (Map<String, Object>) facts.get("methodology");
    }

    public String getResearchArea() {
        if (facts == null) return null;
        return (String) facts.get("research_area");
    }

    @SuppressWarnings("unchecked")
    public java.util.List<String> getKeywords() {
        if (facts == null) return null;
        return (java.util.List<String>) facts.get("keywords");
    }

    @SuppressWarnings("unchecked")
    public java.util.List<String> getKeyFindings() {
        if (facts == null) return null;
        return (java.util.List<String>) facts.get("key_findings");
    }

    @SuppressWarnings("unchecked")
    public java.util.List<String> getContributions() {
        if (facts == null) return null;
        return (java.util.List<String>) facts.get("contributions");
    }

    @SuppressWarnings("unchecked")
    public java.util.List<String> getLimitations() {
        if (facts == null) return null;
        return (java.util.List<String>) facts.get("limitations");
    }

    public String getFutureWork() {
        if (facts == null) return null;
        return (String) facts.get("future_work");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getCitations() {
        if (facts == null) return null;
        return (Map<String, Object>) facts.get("citations");
    }
}
