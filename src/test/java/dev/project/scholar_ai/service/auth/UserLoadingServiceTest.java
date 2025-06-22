package dev.project.scholar_ai.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.project.scholar_ai.model.core.auth.AuthUser;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserLoadingServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @InjectMocks
    private UserLoadingService userLoadingService;

    private AuthUser mockUser;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "encodedPassword123";
    private static final String TEST_ROLE = "ROLE_USER";

    @BeforeEach
    void setUp() {
        mockUser = new AuthUser();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(TEST_EMAIL);
        mockUser.setEncryptedPassword(TEST_PASSWORD);
        mockUser.setRole(TEST_ROLE);
    }

    @Test
    void loadUserByUsername_WithValidEmail_ShouldReturnUserDetails() {
        // Given
        when(authUserRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser));

        // When
        UserDetails userDetails = userLoadingService.loadUserByUsername(TEST_EMAIL);

        // Then
        assertNotNull(userDetails);
        assertEquals(TEST_EMAIL, userDetails.getUsername());
        assertEquals(TEST_PASSWORD, userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(TEST_ROLE)));
        verify(authUserRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    void loadUserByUsername_WithNonExistentEmail_ShouldThrowException() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(authUserRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class, () -> userLoadingService.loadUserByUsername(nonExistentEmail));

        assertEquals("No user found with email: " + nonExistentEmail, exception.getMessage());
        verify(authUserRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void loadUserByUsername_WithNullEmail_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userLoadingService.loadUserByUsername(null));

        verify(authUserRepository, never()).findByEmail(any());
    }

    @Test
    void loadUserByUsername_WithEmptyEmail_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userLoadingService.loadUserByUsername(""));

        verify(authUserRepository, never()).findByEmail(any());
    }
}
