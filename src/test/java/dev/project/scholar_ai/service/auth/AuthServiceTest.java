package dev.project.scholar_ai.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.model.core.auth.AuthUser;
import dev.project.scholar_ai.repository.core.account.UserAccountRepository;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.repository.core.auth.SocialUserRepository;
import dev.project.scholar_ai.security.JwtUtils;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private SocialUserRepository socialUserRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserLoadingService userLoadingService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_ENCODED_PASSWORD = "encodedPassword123";
    private static final String TEST_ACCESS_TOKEN = "access-token-123";
    private static final String TEST_REFRESH_TOKEN = "refresh-token-123";
    private static final String TEST_RESET_CODE = "123456";
    private static final UUID TEST_USER_ID = UUID.randomUUID();

    @Test
    void registerUser_Success() {
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(socialUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);

        AuthUser savedUser = new AuthUser();
        savedUser.setId(TEST_USER_ID);
        savedUser.setEmail(TEST_EMAIL);
        savedUser.setEncryptedPassword(TEST_ENCODED_PASSWORD);
        savedUser.setRole("USER");
        when(authUserRepository.save(any(AuthUser.class))).thenReturn(savedUser);

        authService.registerUser(TEST_EMAIL, TEST_PASSWORD);

        ArgumentCaptor<AuthUser> userCaptor = ArgumentCaptor.forClass(AuthUser.class);
        verify(authUserRepository).save(userCaptor.capture());
        AuthUser capturedUser = userCaptor.getValue();
        assertEquals(TEST_EMAIL, capturedUser.getEmail());
        assertEquals(TEST_ENCODED_PASSWORD, capturedUser.getEncryptedPassword());
        assertEquals("USER", capturedUser.getRole());

        verify(userAccountRepository).save(any());
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(new AuthUser()));

        assertThrows(BadCredentialsException.class, () -> authService.registerUser(TEST_EMAIL, TEST_PASSWORD));
        verify(authUserRepository, never()).save(any());
    }

    @Test
    void loginUser_Success() {
        AuthUser authUser = new AuthUser();
        authUser.setId(TEST_USER_ID);
        authUser.setEmail(TEST_EMAIL);
        authUser.setEncryptedPassword(TEST_ENCODED_PASSWORD);
        authUser.setRole("USER");

        UserDetails userDetails = new User(
                TEST_EMAIL, TEST_ENCODED_PASSWORD, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        when(userLoadingService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD)).thenReturn(true);
        when(jwtUtils.generateAccessToken(TEST_EMAIL)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtUtils.generateRefreshToken(TEST_EMAIL)).thenReturn(TEST_REFRESH_TOKEN);
        when(socialUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(authUser));

        AuthResponse response = authService.loginUser(TEST_EMAIL, TEST_PASSWORD);

        assertNotNull(response);
        assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken());
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals(TEST_USER_ID, response.getUserId());
        assertEquals(1, response.getRoles().size());
        assertEquals("ROLE_USER", response.getRoles().get(0));

        verify(refreshTokenService).saveRefreshToken(TEST_EMAIL, TEST_REFRESH_TOKEN);
    }

    @Test
    void loginUser_InvalidPassword() {
        UserDetails userDetails = new User(
                TEST_EMAIL, TEST_ENCODED_PASSWORD, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        when(userLoadingService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.loginUser(TEST_EMAIL, TEST_PASSWORD));
    }

    @Test
    void refreshToken_Success() {
        AuthUser authUser = new AuthUser();
        authUser.setId(TEST_USER_ID);
        authUser.setEmail(TEST_EMAIL);
        authUser.setRole("USER");

        UserDetails userDetails = new User(
                TEST_EMAIL, TEST_ENCODED_PASSWORD, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtUtils.validateJwtToken(TEST_REFRESH_TOKEN)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(TEST_REFRESH_TOKEN)).thenReturn(TEST_EMAIL);
        when(refreshTokenService.isRefreshTokenValid(TEST_EMAIL, TEST_REFRESH_TOKEN))
                .thenReturn(true);
        when(jwtUtils.generateAccessToken(TEST_EMAIL)).thenReturn(TEST_ACCESS_TOKEN);
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(authUser));
        when(userLoadingService.loadUserByUsername(TEST_EMAIL)).thenReturn(userDetails);

        AuthResponse response = authService.refreshToken(TEST_REFRESH_TOKEN);

        assertNotNull(response);
        assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken());
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals(TEST_USER_ID, response.getUserId());
        assertEquals(1, response.getRoles().size());
        assertEquals("ROLE_USER", response.getRoles().get(0));

        verify(refreshTokenService).saveRefreshToken(TEST_EMAIL, TEST_REFRESH_TOKEN);
    }

    @Test
    void refreshToken_InvalidToken() {
        when(jwtUtils.validateJwtToken(TEST_REFRESH_TOKEN)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(TEST_REFRESH_TOKEN));
    }

    @Test
    void logoutUser_Success() {
        authService.logoutUser(TEST_EMAIL);

        verify(refreshTokenService).deleteRefreshToken(TEST_EMAIL);
    }

    @Test
    void sendResetCodeByMail_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AuthUser user = new AuthUser();
        user.setEmail(TEST_EMAIL);
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));

        authService.sendResetCodeByMail(TEST_EMAIL);

        verify(valueOperations).set(eq("RESET_CODE:" + TEST_EMAIL), anyString(), eq(Duration.ofMinutes(10)));
        verify(emailService).sendResetCodeByEmail(eq(TEST_EMAIL), anyString());
    }

    @Test
    void sendResetCodeByMail_UserNotFound() {
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.sendResetCodeByMail(TEST_EMAIL));
        verify(emailService, never()).sendResetCodeByEmail(anyString(), anyString());
    }

    @Test
    void verifyCodeAndResetPassword_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AuthUser user = new AuthUser();
        user.setEmail(TEST_EMAIL);
        user.setEncryptedPassword(TEST_ENCODED_PASSWORD);

        when(valueOperations.get("RESET_CODE:" + TEST_EMAIL)).thenReturn(TEST_RESET_CODE);
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("newEncodedPassword");

        authService.verifyCodeAndResetPassword(TEST_EMAIL, TEST_RESET_CODE, TEST_PASSWORD);

        verify(authUserRepository).saveAndFlush(user);
        verify(redisTemplate).delete("RESET_CODE:" + TEST_EMAIL);
        verify(redisTemplate).delete("REFRESH_TOKEN:" + TEST_EMAIL);
    }

    @Test
    void verifyCodeAndResetPassword_InvalidCode() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("RESET_CODE:" + TEST_EMAIL)).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.verifyCodeAndResetPassword(TEST_EMAIL, TEST_RESET_CODE, TEST_PASSWORD));
        verify(authUserRepository, never()).saveAndFlush(any());
    }
}
