package dev.project.scholar_ai.repository.auth;

import dev.project.scholar_ai.model.auth.SocialUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialUserRepository extends JpaRepository<SocialUser, UUID> {
    Optional<SocialUser> findByEmailAndProvider(String email, String provider);
}
