package dev.project.scholar_ai.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private static final String TEST_USERNAME = "test@example.com";
    private static final String TEST_REFRESH_TOKEN = "test-refresh-token";
    private static final String REDIS_KEY = "refresh_token" + TEST_USERNAME;
    private static final long REFRESH_TOKEN_VALIDITY_MS = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenValidityMs", REFRESH_TOKEN_VALIDITY_MS);
    }

    @Test
    void saveRefreshToken_ShouldSaveToRedis() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        refreshTokenService.saveRefreshToken(TEST_USERNAME, TEST_REFRESH_TOKEN);

        // Then
        verify(redisTemplate).opsForValue();
        verify(valueOperations)
                .set(eq(REDIS_KEY), eq(TEST_REFRESH_TOKEN), eq(Duration.ofMillis(REFRESH_TOKEN_VALIDITY_MS)));
    }

    @Test
    void getRefreshToken_WhenTokenExists_ShouldReturnToken() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REDIS_KEY)).thenReturn(TEST_REFRESH_TOKEN);

        // When
        String result = refreshTokenService.getRefreshToken(TEST_USERNAME);

        // Then
        assertEquals(TEST_REFRESH_TOKEN, result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(REDIS_KEY);
    }

    @Test
    void getRefreshToken_WhenTokenDoesNotExist_ShouldReturnNull() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);

        // When
        String result = refreshTokenService.getRefreshToken(TEST_USERNAME);

        // Then
        assertNull(result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(REDIS_KEY);
    }

    @Test
    void deleteRefreshToken_ShouldDeleteFromRedis() {
        // When
        refreshTokenService.deleteRefreshToken(TEST_USERNAME);

        // Then
        verify(redisTemplate).delete(REDIS_KEY);
    }

    @Test
    void isRefreshTokenValid_WithValidToken_ShouldReturnTrue() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REDIS_KEY)).thenReturn(TEST_REFRESH_TOKEN);

        // When
        boolean result = refreshTokenService.isRefreshTokenValid(TEST_USERNAME, TEST_REFRESH_TOKEN);

        // Then
        assertTrue(result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(REDIS_KEY);
    }

    @Test
    void isRefreshTokenValid_WithInvalidToken_ShouldReturnFalse() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REDIS_KEY)).thenReturn(TEST_REFRESH_TOKEN);

        // When
        boolean result = refreshTokenService.isRefreshTokenValid(TEST_USERNAME, "invalid-token");

        // Then
        assertFalse(result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(REDIS_KEY);
    }

    @Test
    void isRefreshTokenValid_WithNonExistentToken_ShouldReturnFalse() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);

        // When
        boolean result = refreshTokenService.isRefreshTokenValid(TEST_USERNAME, TEST_REFRESH_TOKEN);

        // Then
        assertFalse(result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(REDIS_KEY);
    }

    @Test
    void saveRefreshToken_WithNullUsername_ShouldThrowException() {
        // When & Then
        assertThrows(
                IllegalArgumentException.class, () -> refreshTokenService.saveRefreshToken(null, TEST_REFRESH_TOKEN));
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void saveRefreshToken_WithNullToken_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> refreshTokenService.saveRefreshToken(TEST_USERNAME, null));
        verify(redisTemplate, never()).opsForValue();
    }
}
