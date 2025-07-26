package dev.project.scholar_ai.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.dto.auth.GitHubEmailDTO;
import dev.project.scholar_ai.dto.auth.GitHubUserDTO;
import dev.project.scholar_ai.model.core.account.UserAccount;
import dev.project.scholar_ai.model.core.auth.AuthUser;
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
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;


    @Value("${spring.github.client-id}")
    private String githubClientId;

    @Value("${spring.github.client-secret}")
    private String githubClientSecret;

    @Value("${spring.github.redirect-uri}")
    private String githubRedirectUri;

    // login by google
//    public AuthResponse loginWithGoogle(String idTokenString) {
//        GoogleIdToken.Payload payload = googleVerifierUtil.verify(idTokenString);
//
//        if (payload == null) {
//            throw new BadCredentialsException("Invalid Google ID token");
//        }
//
//        String email = payload.getEmail();
//        String providerId = payload.getSubject();
//        String name = (String) payload.get("name");
//
//        if (authUserRepository.findByEmail(email).isPresent())
//            throw new BadCredentialsException(
//                    "This email is registered with a password. Please log in using email and password.");
//        else if (socialUserRepository.findByEmail(email).isPresent()) {
//            SocialUser socialUser = socialUserRepository.findByEmail(email).get();
//            if (socialUser.getProvider().equals("GITHUB"))
//                throw new BadCredentialsException(
//                        "This " + email + " is registered with GitHub. Please log in using GitHub.");
//        }
//
//        if (email == null || email.isEmpty()) {
//            throw new IllegalArgumentException("Email not found in Google ID token payload.");
//        }
//
//        SocialUser user = socialUserRepository.findByEmail(email).orElseGet(() -> {
//            SocialUser newUser = new SocialUser();
//            newUser.setEmail(email);
//            newUser.setName(name);
//            newUser.setRole("USER");
//            newUser.setProvider("GOOGLE");
//            return socialUserRepository.save(newUser);
//        });
//
//        if (name != null && !name.equals(user.getName())) {
//            user.setName(name);
//            socialUserRepository.save(user);
//        }
//
//        // Create or update linked user account
//        UserAccount account = userAccountRepository.findByEmail(user.getEmail()).orElse(null);
//
//        if (account == null) {
//            account = new UserAccount();
//            account.setId(user.getId());
//            account.setEmail(user.getEmail());
//            account.setCreatedAt(Instant.now());
//        }
//
//        account.setUpdatedAt(Instant.now());
//        userAccountRepository.save(account);
//
//        // Generate tokens
//        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
//        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
//        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);
//
//        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getId(), List.of(user.getRole()));
//    }


    //new
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

        if (authUserRepository.findByEmail(email).isPresent())
        {
            AuthUser authUser = authUserRepository.findByEmail(email).get();
            if(authUser.getProvider().equalsIgnoreCase("PASSWORD_USER"))
                throw new BadCredentialsException(
                        "This email is registered with a password. Please log in using email and password.");
            else if(authUser.getProvider().equalsIgnoreCase("GITHUB_USER"))
                throw new BadCredentialsException(
                        "This " + email + " is already registered with GitHub!");
            else if(authUser.getProvider().equalsIgnoreCase("GOOGLE_USER")){
                return buildTokensForUser(authUser);
            }
        }


        String randomPassword = UUID.randomUUID().toString(); // generates something like "9f1f0c4a-7bfb-4ebd-8a5e-52c893b7d2c0"
        String encodedPassword = passwordEncoder.encode(randomPassword);
        AuthUser newUser = new AuthUser();
        newUser.setEmail(email);
        newUser.setEncryptedPassword(encodedPassword);
        newUser.setRole("USER");
        newUser.setProvider("GOOGLE_USER"); // Default role
        AuthUser savedUser = authUserRepository.save(newUser);

        // Create or update linked user account
        UserAccount account = userAccountRepository.findByEmail(savedUser.getEmail()).orElse(null);

        if (account == null) {
            account = new UserAccount();
            account.setId(savedUser.getId());
            account.setEmail(savedUser.getEmail());
            account.setCreatedAt(Instant.now());
        }

        account.setUpdatedAt(Instant.now());
        userAccountRepository.save(account);

        // Generate tokens
        String accessToken = jwtUtils.generateAccessToken(savedUser.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(savedUser.getEmail());
        refreshTokenService.saveRefreshToken(savedUser.getEmail(), refreshToken);

        return new AuthResponse(accessToken, refreshToken, savedUser.getEmail(), savedUser.getId(), List.of(savedUser.getRole()));
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

        if (authUserRepository.findByEmail(email).isPresent()){
            AuthUser socialUser = authUserRepository.findByEmail(email).get();
            if(socialUser.getProvider().equalsIgnoreCase("PASSWORD_USER"))
                throw new BadCredentialsException(
                        "This email is already registered with a password!");
            else if (socialUser.getProvider().equalsIgnoreCase("GOOGLE_USER")) {
            throw new BadCredentialsException(
                    "This " + email + " is already registered with Google.");
            }
            else if(socialUser.getProvider().equalsIgnoreCase("GITHUB_USER")){
                return buildTokensForUser(socialUser);
            }
            }

        String randomPassword = UUID.randomUUID().toString(); // generates something like "9f1f0c4a-7bfb-4ebd-8a5e-52c893b7d2c0"
        String encodedPassword = passwordEncoder.encode(randomPassword);

        AuthUser newUser = new AuthUser();
        newUser.setEmail(email);
        newUser.setEncryptedPassword(encodedPassword);
        newUser.setRole("USER");
        newUser.setProvider("GITHUB_USER"); // Default role
        AuthUser savedUser = authUserRepository.save(newUser);

        // Create or update linked user account
        UserAccount account =
                userAccountRepository.findByEmail(savedUser.getEmail()).orElse(null);

        if (account == null) {
            account = new UserAccount();
            account.setId(savedUser.getId());
            account.setEmail(savedUser.getEmail());
            account.setCreatedAt(Instant.now());
        }

        account.setUpdatedAt(Instant.now());
        userAccountRepository.save(account);

        // Generate tokens
        String jwtAccessToken = jwtUtils.generateAccessToken(savedUser.getEmail());
        String jwtRefreshToken = jwtUtils.generateRefreshToken(savedUser.getEmail());
        refreshTokenService.saveRefreshToken(savedUser.getEmail(), jwtRefreshToken);

        return new AuthResponse(
                jwtAccessToken,
                jwtRefreshToken,
                savedUser.getEmail(),
                savedUser.getId(),
                List.of(savedUser.getRole()));
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

    private AuthResponse buildTokensForUser(AuthUser user) {
        String accessToken = jwtUtils.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail());
        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

        // ensure user account exists
        UserAccount account = userAccountRepository.findByEmail(user.getEmail()).orElseGet(() -> {
            UserAccount acc = new UserAccount();
            acc.setId(user.getId());
            acc.setEmail(user.getEmail());
            acc.setCreatedAt(Instant.now());
            return acc;
        });
        account.setUpdatedAt(Instant.now());
        userAccountRepository.save(account);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getId(), List.of(user.getRole()));
    }

}
