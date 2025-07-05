package dev.project.scholar_ai.repository.core.account;

import dev.project.scholar_ai.model.core.account.UserAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByEmail(String email);
}
