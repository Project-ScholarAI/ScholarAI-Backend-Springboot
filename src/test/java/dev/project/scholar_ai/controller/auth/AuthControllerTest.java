package dev.project.scholar_ai.controller.auth;

import static org.junit.jupiter.api.Assertions.*;

import dev.project.scholar_ai.base.BaseIntegrationTest;
import dev.project.scholar_ai.dto.auth.AuthDTO;
import dev.project.scholar_ai.service.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;

class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private AuthService authService;

    private AuthDTO validAuthDTO;

    @BeforeEach
    void setUp() {
        validAuthDTO = new AuthDTO();
        validAuthDTO.setEmail("test@example.com");
        validAuthDTO.setPassword("password123");
    }

    @Test
    void register_WithValidCredentials_ShouldReturnCreated() {}

    @Test
    void register_WithInvalidEmail_ShouldReturnBadRequest() {}

    @Test
    void register_WithExistingEmail_ShouldReturnBadRequest() {}
}
