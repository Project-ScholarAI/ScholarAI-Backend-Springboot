package dev.project.scholar_ai.controller.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.service.auth.SocialAuthService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:testdb",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
            "spring.jpa.hibernate.ddl-auto=create-drop"
        })
class SocialAuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private SocialAuthService socialAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthResponse validAuthResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        validAuthResponse = new AuthResponse();
        validAuthResponse.setAccessToken("access-token");
        validAuthResponse.setRefreshToken("refresh-token");
        validAuthResponse.setUserId(UUID.randomUUID());
        validAuthResponse.setEmail("test@example.com");
        validAuthResponse.setRoles(List.of("USER"));
    }

    // Google Login Tests
    @Test
    void loginWithGoogle_WithValidToken_ShouldReturnOk() throws Exception {
        when(socialAuthService.loginWithGoogle("valid-token")).thenReturn(validAuthResponse);

        Map<String, String> payload = new HashMap<>();
        payload.put("idToken", "valid-token");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/social/google-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    @Test
    void loginWithGoogle_WithMissingToken_ShouldReturnBadRequest() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("idToken", "");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/social/google-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID token is missing."));
    }

    @Test
    void loginWithGoogle_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        when(socialAuthService.loginWithGoogle("invalid-token"))
                .thenThrow(new BadCredentialsException("Invalid Google ID token"));

        Map<String, String> payload = new HashMap<>();
        payload.put("idToken", "invalid-token");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/social/google-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Google login failed: Invalid Google ID token"));
    }

    @Test
    void loginWithGoogle_WithIllegalArgumentException_ShouldReturnUnauthorized() throws Exception {
        when(socialAuthService.loginWithGoogle("invalid-token"))
                .thenThrow(new IllegalArgumentException("Token validation failed"));

        Map<String, String> payload = new HashMap<>();
        payload.put("idToken", "invalid-token");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/social/google-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Google ID token: Token validation failed"));
    }

    // GitHub Login Tests
    @Test
    void loginWithGithub_WithValidCode_ShouldReturnOk() throws Exception {
        when(socialAuthService.loginWithGithub(anyString())).thenReturn(validAuthResponse);

        Map<String, String> payload = new HashMap<>();
        payload.put("code", "valid-code");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/social/github-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    @Test
    void loginWithGithub_WithMissingCode_ShouldReturnBadRequest() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("code", "");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/social/github-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("GitHub authorization code is missing"));
    }

    @Test
    void loginWithGithub_WithInvalidCode_ShouldReturnUnauthorized() throws Exception {
        when(socialAuthService.loginWithGithub("invalid-code"))
                .thenThrow(new BadCredentialsException("Invalid GitHub code"));

        Map<String, String> payload = new HashMap<>();
        payload.put("code", "invalid-code");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/social/github-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("GitHub login failed: Invalid GitHub code"));
    }

    @Test
    void loginWithGithub_WithIllegalStateException_ShouldReturnBadRequest() throws Exception {
        when(socialAuthService.loginWithGithub("bad-code"))
                .thenThrow(new IllegalStateException("Token exchange failed"));

        Map<String, String> payload = new HashMap<>();
        payload.put("code", "bad-code");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/social/github-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.message").value("Failed to get access token from GitHub: Token exchange failed"));
    }
}
