package dev.project.scholar_ai.repository.core.auth;

import dev.project.scholar_ai.model.core.auth.AuthUser;
import dev.project.scholar_ai.model.core.auth.PasswordResetToken;
import java.util.Optional;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByUserAndToken(User user, String token);

    void deleteAllByUser(AuthUser user);
}
