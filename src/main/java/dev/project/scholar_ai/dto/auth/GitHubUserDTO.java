package dev.project.scholar_ai.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitHubUserDTO {
    private Long id; // GitHub's user ID is a number

    private String login;
    private String name;
    private String email;
}
