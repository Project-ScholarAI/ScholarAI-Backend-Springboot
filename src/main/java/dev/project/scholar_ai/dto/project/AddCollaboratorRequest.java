package dev.project.scholar_ai.dto.project;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddCollaboratorRequest(
        @NotBlank(message = "Collaborator email is required") @Email(message = "Invalid email format")
                String collaboratorEmail,
        @NotNull(message = "Collaboration role is required") String role) {}
