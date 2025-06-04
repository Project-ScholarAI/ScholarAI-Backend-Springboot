package dev.project.scholar_ai.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.model.auth.SocialUser;
import dev.project.scholar_ai.repository.auth.SocialUserRepository;
import dev.project.scholar_ai.security.GoogleVerifierUtil;
import dev.project.scholar_ai.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialAuthService {
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final SocialUserRepository socialUserRepository;
    @Autowired
    private GoogleVerifierUtil googleVerifierUtil;


    // login by google
    public AuthResponse loginWithGoogle(String idTokenString) {
        GoogleIdToken.Payload payload = googleVerifierUtil.verify(idTokenString);

        if (payload == null) {
            throw new BadCredentialsException("Invalid Google ID token");
        }

        String email = payload.getEmail();
        String providerId = payload.getSubject();
        String name = (String) payload.get("name");

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not found in Google ID token payload.");
        }

        SocialUser user = socialUserRepository
                .findByEmailAndProvider(email, "GOOGLE")
                .map(existingUser -> {
                    if (!providerId.equals(existingUser.getProviderId())) {
                        throw new BadCredentialsException("Google account mismatch for this email.");
                    }

                    // Update name if changed
                    if (name != null && !name.equals(existingUser.getName())) {
                        existingUser.setName(name);
                        return socialUserRepository.save(existingUser);
                    }

                    return existingUser;
                })
                .orElseGet(() -> {
                    SocialUser newUser = new SocialUser();
                    newUser.setEmail(email);
                    newUser.setProvider("GOOGLE");
                    newUser.setProviderId(providerId);
                    newUser.setName(name);
                    newUser.setRole("USER");
                    return socialUserRepository.save(newUser);
                });

        // Generate tokens
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getId(), List.of(user.getRole()));
    }




}
