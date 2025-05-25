package dev.project.scholar_ai.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String refreshToken;
}
