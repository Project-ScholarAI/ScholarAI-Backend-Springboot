package dev.project.scholar_ai.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword";

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
    }

    @Test
    void registerUser_WithNewEmail_ShouldSucceed() {}

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {}

    @Test
    void registerUser_WithInvalidEmail_ShouldThrowException() {}

    @Test
    void registerUser_WithShortPassword_ShouldThrowException() {}
}
