package dev.project.scholar_ai.model.auth;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "social_users")
public class SocialUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;

    private String role;

    @OneToMany(mappedBy = "socialUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserProvider> providers;

}
