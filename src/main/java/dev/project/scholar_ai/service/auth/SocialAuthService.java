package dev.project.scholar_ai.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.dto.auth.GitHubEmailDTO;
import dev.project.scholar_ai.dto.auth.GitHubUserDTO;
import dev.project.scholar_ai.model.core.auth.SocialUser;
import dev.project.scholar_ai.model.core.auth.UserProvider;
import dev.project.scholar_ai.repository.core.auth.SocialUserRepository;
import dev.project.scholar_ai.repository.core.auth.UserProviderRepository;
import dev.project.scholar_ai.security.GoogleVerifierUtil;
import dev.project.scholar_ai.security.JwtUtils;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SocialAuthService {
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final SocialUserRepository socialUserRepository;
    private final UserProviderRepository userProviderRepository;
    private final GoogleVerifierUtil googleVerifierUtil;
    private final RestTemplate restTemplate;

    @Value("${spring.github.client-id}")
    private String githubClientId;

    @Value("${spring.github.client-secret}")
    private String githubClientSecret;

    @Value("${spring.github.redirect-uri}")
    private String githubRedirectUri;

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

        SocialUser user = socialUserRepository.findByEmail(email).orElseGet(() -> {
            SocialUser newUser = new SocialUser();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setRole("USER");
            return socialUserRepository.save(newUser);
        });

        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
            socialUserRepository.save(user);
        }

        userProviderRepository.findBySocialUserAndProvider(user, "GOOGLE").orElseGet(() -> {
            UserProvider provider = new UserProvider();
            provider.setSocialUser(user);
            provider.setProvider("GOOGLE");
            provider.setProviderId(providerId);
            return userProviderRepository.save(provider);
        });

        // Generate tokens
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getId(), List.of(user.getRole()));
    }

    // login with github
    public AuthResponse loginWithGithub(String code) {

        // Exchange code for access token
        String accessToken = exchangeCodeForAccessToken(code);

        // Get github user profile
        GitHubUserDTO gitHubUser = fetchGitHubUser(accessToken);

        String email = gitHubUser.getEmail();
        String providerId = gitHubUser.getId().toString();
        String name = gitHubUser.getName() != null ? gitHubUser.getName() : gitHubUser.getLogin();

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not found in Github user profile");
        }

        SocialUser socialUser = socialUserRepository.findByEmail(email).orElseGet(() -> {
            SocialUser newUser = new SocialUser();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setRole("USER");
            return socialUserRepository.save(newUser);
        });

        if (name != null && !name.equals(socialUser.getName())) {
            socialUser.setName(name);
            socialUserRepository.save(socialUser);
        }

        userProviderRepository.findBySocialUserAndProvider(socialUser, "GITHUB").orElseGet(() -> {
            UserProvider provider = new UserProvider();
            provider.setSocialUser(socialUser);
            provider.setProvider("GITHUB");
            provider.setProviderId(providerId);
            return userProviderRepository.save(provider);
        });

        // Generate tokens
        String jwtAccessToken = jwtUtils.generateAccessToken(socialUser.getEmail());
        String jwtRefreshToken = jwtUtils.generateRefreshToken(socialUser.getEmail());
        refreshTokenService.saveRefreshToken(socialUser.getEmail(), jwtRefreshToken);

        return new AuthResponse(
                jwtAccessToken,
                jwtRefreshToken,
                socialUser.getEmail(),
                socialUser.getId(),
                List.of(socialUser.getRole()));
    }

    // exchange code for access token
    public String exchangeCodeForAccessToken(String code) {
        String url = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", githubClientId);
        body.add("client_secret", githubClientSecret);
        body.add("code", code);
        body.add("redirect_uri", githubRedirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        System.out.println("GitHub token exchange response: " + response);

        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            // Log the error response from GitHub if available
            if (response.getBody() != null && response.getBody().containsKey("error_description")) {
                System.err.println(
                        "GitHub token exchange failed: " + response.getBody().get("error_description"));
            }
            throw new IllegalStateException("Failed to get access token from GitHub");
        }

        return (String) response.getBody().get("access_token");
    }

    // fetch github user
    private GitHubUserDTO fetchGitHubUser(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<GitHubUserDTO> response =
                restTemplate.exchange("https://api.github.com/user", HttpMethod.GET, entity, GitHubUserDTO.class);

        System.out.println("GitHub /user raw response: " + response.getBody());

        GitHubUserDTO userDTO = response.getBody();

        // Fetch email if null
        if (userDTO.getEmail() == null) {
            ResponseEntity<GitHubEmailDTO[]> emailResponse = restTemplate.exchange(
                    "https://api.github.com/user/emails", HttpMethod.GET, entity, GitHubEmailDTO[].class);

            for (GitHubEmailDTO mail : emailResponse.getBody()) {
                if (mail.isPrimary() && mail.isVerified()) {
                    userDTO.setEmail(mail.getEmail());
                    break;
                }
            }
        }

        return userDTO;
    }
}
