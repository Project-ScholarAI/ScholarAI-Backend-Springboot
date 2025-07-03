package dev.project.scholar_ai.service.auth;

import dev.project.scholar_ai.model.core.auth.AuthUser;
import dev.project.scholar_ai.model.core.auth.SocialUser;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.repository.core.auth.SocialUserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(transactionManager = "transactionManager")
public class UserLoadingService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;
    private final SocialUserRepository socialUserRepository;

    public UserLoadingService(AuthUserRepository authUserRepository, SocialUserRepository socialUserRepository) {
        this.authUserRepository = authUserRepository;
        this.socialUserRepository = socialUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        Optional<AuthUser> authUserOptional = authUserRepository.findByEmail(username);
        if (authUserOptional.isPresent()) {
            AuthUser user = authUserOptional.get();
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRole());
            List<GrantedAuthority> authorityList = List.of(grantedAuthority);
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), user.getEncryptedPassword(), authorityList);
        }

        Optional<SocialUser> socialUserOptional = socialUserRepository.findByEmail(username);
        if (socialUserOptional.isPresent()) {
            SocialUser user = socialUserOptional.get();
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRole());
            List<GrantedAuthority> authorityList = List.of(grantedAuthority);
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), "", authorityList); // No password for social users
        }

        throw new UsernameNotFoundException("No user found with email: " + username);
    }
}
