package dev.project.scholar_ai.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SocialAuthServiceTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private SocialUserRepository socialUserRepository;

    @Mock
    private UserProviderRepository userProviderRepository;

    @Mock
    private GoogleVerifierUtil googleVerifierUtil;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SocialAuthService socialAuthService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_PROVIDER_ID = "123456789";
    private static final String TEST_ACCESS_TOKEN = "test-access-token";
    private static final String TEST_REFRESH_TOKEN = "test-refresh-token";
    private static final String TEST_GITHUB_CODE = "github-code";
    private static final String TEST_GITHUB_TOKEN = "github-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(socialAuthService, "githubClientId", "test-client-id");
        ReflectionTestUtils.setField(socialAuthService, "githubClientSecret", "test-client-secret");
        ReflectionTestUtils.setField(socialAuthService, "githubRedirectUri", "http://localhost:8080/callback");
    }

    @Test
    void loginWithGoogle_NewUser_ShouldCreateUserAndReturnTokens() {
        // Given
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn(TEST_EMAIL);
        when(payload.getSubject()).thenReturn(TEST_PROVIDER_ID);
        when(payload.get("name")).thenReturn(TEST_NAME);
        when(googleVerifierUtil.verify(anyString())).thenReturn(payload);

        when(socialUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(socialUserRepository.save(any(SocialUser.class))).thenAnswer(invocation -> {
            SocialUser user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        when(userProviderRepository.findBySocialUserAndProvider(any(), eq("GOOGLE")))
                .thenReturn(Optional.empty());
        when(userProviderRepository.save(any(UserProvider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtUtils.generateAccessToken(TEST_EMAIL)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtUtils.generateRefreshToken(TEST_EMAIL)).thenReturn(TEST_REFRESH_TOKEN);

        // When
        AuthResponse response = socialAuthService.loginWithGoogle("google-token");

        // Then
        assertNotNull(response);
        assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken());
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals(List.of("USER"), response.getRoles());

        verify(socialUserRepository).save(any(SocialUser.class));
        verify(userProviderRepository).save(any(UserProvider.class));
        verify(refreshTokenService).saveRefreshToken(TEST_EMAIL, TEST_REFRESH_TOKEN);
    }

    @Test
    void loginWithGoogle_ExistingUser_ShouldUpdateNameAndReturnTokens() {
        // Given
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn(TEST_EMAIL);
        when(payload.getSubject()).thenReturn(TEST_PROVIDER_ID);
        when(payload.get("name")).thenReturn("Updated Name");
        when(googleVerifierUtil.verify(anyString())).thenReturn(payload);

        SocialUser existingUser = new SocialUser();
        existingUser.setId(UUID.randomUUID());
        existingUser.setEmail(TEST_EMAIL);
        existingUser.setName(TEST_NAME);
        existingUser.setRole("USER");

        when(socialUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingUser));
        when(socialUserRepository.save(any(SocialUser.class))).thenReturn(existingUser);

        UserProvider existingProvider = new UserProvider();
        existingProvider.setSocialUser(existingUser);
        existingProvider.setProvider("GOOGLE");
        existingProvider.setProviderId(TEST_PROVIDER_ID);

        when(userProviderRepository.findBySocialUserAndProvider(any(), eq("GOOGLE")))
                .thenReturn(Optional.of(existingProvider));

        when(jwtUtils.generateAccessToken(TEST_EMAIL)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtUtils.generateRefreshToken(TEST_EMAIL)).thenReturn(TEST_REFRESH_TOKEN);

        // When
        AuthResponse response = socialAuthService.loginWithGoogle("google-token");

        // Then
        assertNotNull(response);
        assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken());
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals("Updated Name", existingUser.getName());

        verify(socialUserRepository).save(existingUser);
        verify(refreshTokenService).saveRefreshToken(TEST_EMAIL, TEST_REFRESH_TOKEN);
    }

    @Test
    void loginWithGoogle_InvalidToken_ShouldThrowException() {
        // Given
        when(googleVerifierUtil.verify(anyString())).thenReturn(null);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> socialAuthService.loginWithGoogle("invalid-token"));
    }

    @Test
    void loginWithGithub_NewUser_ShouldCreateUserAndReturnTokens() {
        // Given
        GitHubUserDTO githubUser = new GitHubUserDTO();
        githubUser.setId(Long.parseLong(TEST_PROVIDER_ID));
        githubUser.setEmail(TEST_EMAIL);
        githubUser.setName(TEST_NAME);

        // Mock token exchange
        ResponseEntity<Map> tokenResponse =
                new ResponseEntity<>(Map.of("access_token", TEST_GITHUB_TOKEN), HttpStatus.OK);
        when(restTemplate.postForEntity(eq("https://github.com/login/oauth/access_token"), any(), eq(Map.class)))
                .thenReturn(tokenResponse);

        // Mock user profile fetch
        ResponseEntity<GitHubUserDTO> userResponse = new ResponseEntity<>(githubUser, HttpStatus.OK);
        when(restTemplate.exchange(
                        eq("https://api.github.com/user"), eq(HttpMethod.GET), any(), eq(GitHubUserDTO.class)))
                .thenReturn(userResponse);

        when(socialUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(socialUserRepository.save(any(SocialUser.class))).thenAnswer(invocation -> {
            SocialUser user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        when(userProviderRepository.findBySocialUserAndProvider(any(), eq("GITHUB")))
                .thenReturn(Optional.empty());
        when(userProviderRepository.save(any(UserProvider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtUtils.generateAccessToken(TEST_EMAIL)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtUtils.generateRefreshToken(TEST_EMAIL)).thenReturn(TEST_REFRESH_TOKEN);

        // When
        AuthResponse response = socialAuthService.loginWithGithub(TEST_GITHUB_CODE);

        // Then
        assertNotNull(response);
        assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken());
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals(List.of("USER"), response.getRoles());

        verify(socialUserRepository).save(any(SocialUser.class));
        verify(userProviderRepository).save(any(UserProvider.class));
        verify(refreshTokenService).saveRefreshToken(TEST_EMAIL, TEST_REFRESH_TOKEN);
    }

    @Test
    void loginWithGithub_WithPrivateEmail_ShouldFetchEmailAndCreateUser() {
        // Given
        GitHubUserDTO githubUser = new GitHubUserDTO();
        githubUser.setId(Long.parseLong(TEST_PROVIDER_ID));
        githubUser.setEmail(null); // Private email
        githubUser.setName(TEST_NAME);

        GitHubEmailDTO[] emails = new GitHubEmailDTO[] {
            createGitHubEmailDTO("other@example.com", false, true), createGitHubEmailDTO(TEST_EMAIL, true, true)
        };

        // Mock token exchange
        ResponseEntity<Map> tokenResponse =
                new ResponseEntity<>(Map.of("access_token", TEST_GITHUB_TOKEN), HttpStatus.OK);
        when(restTemplate.postForEntity(eq("https://github.com/login/oauth/access_token"), any(), eq(Map.class)))
                .thenReturn(tokenResponse);

        // Mock user profile fetch
        ResponseEntity<GitHubUserDTO> userResponse = new ResponseEntity<>(githubUser, HttpStatus.OK);
        when(restTemplate.exchange(
                        eq("https://api.github.com/user"), eq(HttpMethod.GET), any(), eq(GitHubUserDTO.class)))
                .thenReturn(userResponse);

        // Mock email fetch
        ResponseEntity<GitHubEmailDTO[]> emailResponse = new ResponseEntity<>(emails, HttpStatus.OK);
        when(restTemplate.exchange(
                        eq("https://api.github.com/user/emails"),
                        eq(HttpMethod.GET),
                        any(),
                        eq(GitHubEmailDTO[].class)))
                .thenReturn(emailResponse);

        when(socialUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(socialUserRepository.save(any(SocialUser.class))).thenAnswer(invocation -> {
            SocialUser user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        when(userProviderRepository.findBySocialUserAndProvider(any(), eq("GITHUB")))
                .thenReturn(Optional.empty());
        when(userProviderRepository.save(any(UserProvider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtUtils.generateAccessToken(TEST_EMAIL)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtUtils.generateRefreshToken(TEST_EMAIL)).thenReturn(TEST_REFRESH_TOKEN);

        // When
        AuthResponse response = socialAuthService.loginWithGithub(TEST_GITHUB_CODE);

        // Then
        assertNotNull(response);
        assertEquals(TEST_EMAIL, response.getEmail());
        verify(restTemplate)
                .exchange(
                        eq("https://api.github.com/user/emails"),
                        eq(HttpMethod.GET),
                        any(),
                        eq(GitHubEmailDTO[].class));
    }

    @Test
    void exchangeCodeForAccessToken_InvalidResponse_ShouldThrowException() {
        // Given
        ResponseEntity<Map> response =
                new ResponseEntity<>(Map.of("error_description", "Invalid code"), HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(eq("https://github.com/login/oauth/access_token"), any(), eq(Map.class)))
                .thenReturn(response);

        // When & Then
        assertThrows(IllegalStateException.class, () -> socialAuthService.exchangeCodeForAccessToken(TEST_GITHUB_CODE));
    }

    private GitHubEmailDTO createGitHubEmailDTO(String email, boolean primary, boolean verified) {
        GitHubEmailDTO dto = new GitHubEmailDTO();
        dto.setEmail(email);
        dto.setPrimary(primary);
        dto.setVerified(verified);
        return dto;
    }
}
