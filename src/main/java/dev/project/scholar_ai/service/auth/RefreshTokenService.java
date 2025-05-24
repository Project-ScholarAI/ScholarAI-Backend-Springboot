package dev.project.scholar_ai.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {
    private static final String REDIS_REFRESH_TOKEN_PREFIX = "refresh_token";
    private final RedisTemplate<String, String>redisTemplate;

    @Value("${spring.app.refreshTokenValidityMs}")
    private long refreshTokenValidityMs;

    public RefreshTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRefreshToken(String username, String refreshToken){
        String key = REDIS_REFRESH_TOKEN_PREFIX + username;
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenValidityMs, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String username)
    {
        return redisTemplate.opsForValue().get(REDIS_REFRESH_TOKEN_PREFIX + username);
    }

    public void deleteRefreshToken(String username)
    {
        redisTemplate.delete(REDIS_REFRESH_TOKEN_PREFIX + username);
    }

    public boolean isRefreshTokenValid(String username, String refreshToken){
        String storedToken = getRefreshToken(username);
        return storedToken != null && storedToken.equals(refreshToken);
    }

}
