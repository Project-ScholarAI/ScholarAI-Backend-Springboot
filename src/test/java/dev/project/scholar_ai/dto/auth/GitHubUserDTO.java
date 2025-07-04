package dev.project.scholar_ai.dto.auth;

import lombok.Data;

@Data
public class GitHubUserDTO {
    private Long id;
    private String login;
    private String name;
    private String email;
}
