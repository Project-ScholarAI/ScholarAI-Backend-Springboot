package dev.project.scholar_ai.dto.project;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RemoveCollaboratorRequest(
        @NotBlank(message = "Collaborator email is required") @Email(message = "Invalid email format")
                String collaboratorEmail) {}
