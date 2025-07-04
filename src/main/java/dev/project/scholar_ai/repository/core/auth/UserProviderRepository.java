package dev.project.scholar_ai.repository.core.auth;

import dev.project.scholar_ai.model.core.auth.SocialUser;
import dev.project.scholar_ai.model.core.auth.UserProvider;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProviderRepository extends JpaRepository<UserProvider, UUID> {
    Optional<UserProvider> findBySocialUserAndProvider(SocialUser user, String provider);

    Optional<UserProvider> findByProviderAndProviderId(String provider, String providerId);
}
