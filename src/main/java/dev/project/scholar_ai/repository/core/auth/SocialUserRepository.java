package dev.project.scholar_ai.repository.core.auth;

import dev.project.scholar_ai.model.core.auth.SocialUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialUserRepository extends JpaRepository<SocialUser, UUID> {
    Optional<SocialUser> findByEmail(String email);
}
