package dev.project.scholar_ai.dto.project;

import java.time.Instant;
import java.util.UUID;

public record CollaboratorDto(
        UUID id,
        UUID projectId,
        UUID collaboratorId,
        String collaboratorEmail,
        String collaboratorName,
        String ownerEmail,
        String role,
        Instant createdAt) {}
