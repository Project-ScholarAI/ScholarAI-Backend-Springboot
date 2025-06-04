package dev.project.scholar_ai.repository.auth;

import dev.project.scholar_ai.model.auth.SocialUser;
import dev.project.scholar_ai.model.auth.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProviderRepository extends JpaRepository<UserProvider, Long> {
    Optional<UserProvider> findBySocialUserAndProvider(SocialUser user, String provider);
    Optional<UserProvider> findByProviderAndProviderId(String provider, String providerId);
}
