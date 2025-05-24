package dev.project.scholar_ai.repository.auth;

import dev.project.scholar_ai.model.auth.AuthUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
    Optional<AuthUser> findByEmail(String email);
}
