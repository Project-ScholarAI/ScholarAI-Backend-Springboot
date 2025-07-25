package dev.project.scholar_ai.model.core.project;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@Table(name = "project_collaborators")
public class ProjectCollaborator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "project_id", nullable = false, columnDefinition = "uuid")
    private UUID projectId;

    @Column(name = "collaborator_id", nullable = false, columnDefinition = "uuid")
    private UUID collaboratorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CollaborationRole role = CollaborationRole.VIEWER;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public enum CollaborationRole {
        VIEWER,
        EDITOR,
        ADMIN
    }
}
