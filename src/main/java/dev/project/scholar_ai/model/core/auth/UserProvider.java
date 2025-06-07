package dev.project.scholar_ai.model.core.auth;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_providers", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "provider"}))
public class UserProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private SocialUser socialUser;

    @Column(nullable = false)
    private String provider; // e.g., "GOOGLE", "GITHUB"

    @Column(nullable = false)
    private String providerId; // e.g., Google sub or GitHub ID
}
