package dev.project.scholar_ai.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import dev.project.scholar_ai.service.auth.UserLoadingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserLoadingService userLoadingService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidTokenAndSession_ShouldAuthenticateUser() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        String username = "testuser";
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        when(jwtUtils.getJwtFromHeader(request)).thenReturn(token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn(username);
        when(redisTemplate.hasKey("refresh_token" + username)).thenReturn(true);
        when(userLoadingService.loadUserByUsername(username)).thenReturn(userDetails);

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(
                username, SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithValidTokenButNoSession_ShouldNotAuthenticate() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        String username = "testuser";

        when(jwtUtils.getJwtFromHeader(request)).thenReturn(token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn(username);
        when(redisTemplate.hasKey("refresh_token" + username)).thenReturn(false);
        when(response.getWriter()).thenReturn(writer);

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotAuthenticate() throws ServletException, IOException {
        // Given
        String token = "invalid-token";
        when(jwtUtils.getJwtFromHeader(request)).thenReturn(token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(false);

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNoToken_ShouldNotAuthenticate() throws ServletException, IOException {
        // Given
        when(jwtUtils.getJwtFromHeader(request)).thenReturn(null);

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenUserNotFound_ShouldNotAuthenticate() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        String username = "testuser";

        when(jwtUtils.getJwtFromHeader(request)).thenReturn(token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn(username);
        when(redisTemplate.hasKey("refresh_token" + username)).thenReturn(true);
        when(userLoadingService.loadUserByUsername(username))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // When
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
