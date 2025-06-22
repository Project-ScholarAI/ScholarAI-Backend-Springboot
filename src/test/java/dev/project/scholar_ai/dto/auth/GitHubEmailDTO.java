package dev.project.scholar_ai.dto.auth;

import lombok.Data;

@Data
public class GitHubEmailDTO {
    private String email;
    private boolean primary;
    private boolean verified;
}
