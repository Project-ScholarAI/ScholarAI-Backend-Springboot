package dev.project.scholar_ai.model.core.papercall;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "paper_call")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperCall {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String link;
    
    @Column(nullable = false, length = 50)
    private String type;
    
    @Column(nullable = false, length = 100)
    private String source;
    
    @Column(nullable = false, length = 255)
    private String domain;

    @Column(name = "when_held", length = 255)
    private String whenHeld;

    @Column(name = "where_held", length = 255)
    private String whereHeld;

    @Column(length = 255)
    private String deadline;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 