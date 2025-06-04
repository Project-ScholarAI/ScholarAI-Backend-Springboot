package dev.project.scholar_ai.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitHubEmailDTO {
    private String email;
    private boolean primary;
    private boolean verified;
}
