package dev.project.scholar_ai.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletRequest request;

    private final String jwtSecret = "===========================ScholarAI===========================";
    private final long accessTokenValidityMs = 10000;
    private final long refreshTokenValidityMs = 20000;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtils, "accessTokenValidityMs", accessTokenValidityMs);
        ReflectionTestUtils.setField(jwtUtils, "refreshTokenValidityMs", refreshTokenValidityMs);
    }

    @Test
    void generateAccessToken_ShouldReturnValidToken() {
        String token = jwtUtils.generateAccessToken("testuser");
        assertNotNull(token);
        assertEquals("testuser", jwtUtils.getUserNameFromJwtToken(token));
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtils, "accessTokenValidityMs", 1);
        String token = jwtUtils.generateAccessToken("testuser");
        Thread.sleep(10); // Wait for token to expire
        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    void getUserNameFromJwtToken_WithExpiredToken_ShouldThrowException() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtils, "accessTokenValidityMs", 1);
        String token = jwtUtils.generateAccessToken("testuser");
        Thread.sleep(10); // Wait for token to expire
        assertThrows(ExpiredJwtException.class, () -> jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void validateJwtToken_WithInvalidSignature_ShouldReturnFalse() {
        String token = jwtUtils.generateAccessToken("testuser");
        String invalidToken = token.substring(0, token.length() - 1) + "A";
        assertFalse(jwtUtils.validateJwtToken(invalidToken));
    }

    @Test
    void validateJwtToken_WithMalformedToken_ShouldReturnFalse() {
        assertFalse(jwtUtils.validateJwtToken("malformed-token"));
    }

    @Test
    void getUserNameFromJwtToken_WithMalformedToken_ShouldThrowException() {
        assertThrows(MalformedJwtException.class, () -> jwtUtils.getUserNameFromJwtToken("malformed-token"));
    }

    @Test
    void getJwtFromHeader_WithValidHeader_ShouldReturnToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        assertEquals("test-token", jwtUtils.getJwtFromHeader(request));
    }

    @Test
    void getJwtFromHeader_WithMissingHeader_ShouldReturnNull() {
        when(request.getHeader("Authorization")).thenReturn(null);
        assertNull(jwtUtils.getJwtFromHeader(request));
    }

    @Test
    void getJwtFromHeader_WithInvalidHeader_ShouldReturnNull() {
        when(request.getHeader("Authorization")).thenReturn("Invalid-token");
        assertNull(jwtUtils.getJwtFromHeader(request));
    }
}
