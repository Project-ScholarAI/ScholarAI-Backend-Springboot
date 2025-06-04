package dev.project.scholar_ai.dto.auth;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitHubUserDTO {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;
    private String login;
    private String name;
    private String email;
}
