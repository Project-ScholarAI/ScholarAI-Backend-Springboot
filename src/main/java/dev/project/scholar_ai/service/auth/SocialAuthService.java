package dev.project.scholar_ai.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.dto.auth.GitHubEmailDTO;
import dev.project.scholar_ai.dto.auth.GitHubUserDTO;
import dev.project.scholar_ai.model.core.account.UserAccount;
import dev.project.scholar_ai.model.core.auth.SocialUser;
import dev.project.scholar_ai.repository.core.account.UserAccountRepository;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.repository.core.auth.SocialUserRepository;
import dev.project.scholar_ai.repository.core.auth.UserProviderRepository;
import dev.project.scholar_ai.security.GoogleVerifierUtil;
import dev.project.scholar_ai.security.JwtUtils;
import java.time.Instant;
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
    private final AuthUserRepository authUserRepository;
    private final UserAccountRepository userAccountRepository;

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

        if (authUserRepository.findByEmail(email).isPresent())
            throw new BadCredentialsException(
                    "This email is registered with a password. Please log in using email and password.");
        else if (socialUserRepository.findByEmail(email).isPresent()) {
            SocialUser socialUser = socialUserRepository.findByEmail(email).get();
            if (socialUser.getProvider().equals("GITHUB"))
                throw new BadCredentialsException(
                        "This " + email + " is registered with GitHub. Please log in using GitHub.");
        }

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not found in Google ID token payload.");
        }

        SocialUser user = socialUserRepository.findByEmail(email).orElseGet(() -> {
            SocialUser newUser = new SocialUser();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setRole("USER");
            newUser.setProvider("GOOGLE");
            return socialUserRepository.save(newUser);
        });

        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
            socialUserRepository.save(user);
        }

        // Create or update linked user account
        UserAccount account = userAccountRepository.findByEmail(user.getEmail()).orElse(null);

        if (account == null) {
            account = new UserAccount();
            account.setId(user.getId());
            account.setEmail(user.getEmail());
            account.setCreatedAt(Instant.now());
        }

        account.setUpdatedAt(Instant.now());
        userAccountRepository.save(account);

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

        if (authUserRepository.findByEmail(email).isPresent())
            throw new BadCredentialsException(
                    "This email is registered with a password. Please log in using email and password.");
        else if (socialUserRepository.findByEmail(email).isPresent()) {
            SocialUser socialUser = socialUserRepository.findByEmail(email).get();
            if (socialUser.getProvider().equals("GOOGLE"))
                throw new BadCredentialsException(
                        "This " + email + " is registered with GitHub. Please log in using GitHub.");
        }

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not found in Github user profile");
        }

        SocialUser socialUser = socialUserRepository.findByEmail(email).orElseGet(() -> {
            SocialUser newUser = new SocialUser();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setRole("USER");
            newUser.setProvider("GITHUB");
            return socialUserRepository.save(newUser);
        });

        if (name != null && !name.equals(socialUser.getName())) {
            socialUser.setName(name);
            socialUserRepository.save(socialUser);
        }

        // Create or update linked user account
        UserAccount account =
                userAccountRepository.findByEmail(socialUser.getEmail()).orElse(null);

        if (account == null) {
            account = new UserAccount();
            account.setId(socialUser.getId());
            account.setEmail(socialUser.getEmail());
            account.setCreatedAt(Instant.now());
        }

        account.setUpdatedAt(Instant.now());
        userAccountRepository.save(account);

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
