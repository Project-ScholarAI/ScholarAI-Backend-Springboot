package dev.project.scholar_ai.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleVerifierUtilTest {

    @Mock
    private GoogleIdTokenVerifier verifier;

    private GoogleVerifierUtil googleVerifierUtil;

    @BeforeEach
    void setUp() {
        googleVerifierUtil = new GoogleVerifierUtil(verifier);
    }

    @Test
    void verify_WithValidToken_ShouldReturnPayload() throws GeneralSecurityException, IOException {
        // Given
        String idTokenString = "valid-token";
        GoogleIdToken idToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("test@example.com");

        when(verifier.verify(idTokenString)).thenReturn(idToken);
        when(idToken.getPayload()).thenReturn(payload);

        // When
        GoogleIdToken.Payload result = googleVerifierUtil.verify(idTokenString);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void verify_WithInvalidToken_ShouldReturnNull() throws GeneralSecurityException, IOException {
        // Given
        String idTokenString = "invalid-token";
        when(verifier.verify(idTokenString)).thenReturn(null);

        // When
        GoogleIdToken.Payload result = googleVerifierUtil.verify(idTokenString);

        // Then
        assertNull(result);
    }

    @Test
    void verify_WhenVerifierThrowsException_ShouldReturnNull() throws GeneralSecurityException, IOException {
        // Given
        String idTokenString = "token-causing-exception";
        when(verifier.verify(idTokenString)).thenThrow(new GeneralSecurityException("Verification failed"));

        // When
        GoogleIdToken.Payload result = googleVerifierUtil.verify(idTokenString);

        // Then
        assertNull(result);
    }
}
