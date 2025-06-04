package dev.project.scholar_ai.model.auth;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "social_users")
public class SocialUser {

    @Id // ✅ Correct now
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String provider;

    private String providerId;

    private String name;

    private String role = "USER";
}
