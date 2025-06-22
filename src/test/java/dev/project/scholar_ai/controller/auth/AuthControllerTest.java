package dev.project.scholar_ai.controller.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.project.scholar_ai.dto.auth.AuthDTO;
import dev.project.scholar_ai.dto.auth.AuthResponse;
import dev.project.scholar_ai.dto.common.APIResponse;
import dev.project.scholar_ai.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:testdb",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
            "spring.jpa.hibernate.ddl-auto=create-drop"
        })
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthDTO validAuthDTO;
    private AuthResponse validAuthResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        validAuthDTO = new AuthDTO();
        validAuthDTO.setEmail("test@example.com");
        validAuthDTO.setPassword("password123");

        validAuthResponse = new AuthResponse();
        validAuthResponse.setAccessToken("access-token");
        validAuthResponse.setRefreshToken("refresh-token");
        validAuthResponse.setUserId(UUID.randomUUID());
        validAuthResponse.setEmail("test@example.com");
    }

    // Registration Tests
    @Test
    void register_WithValidCredentials_ShouldReturnCreated() throws Exception {
        doNothing().when(authService).registerUser(validAuthDTO.getEmail(), validAuthDTO.getPassword());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        APIResponse<String> response = objectMapper.readValue(
                responseBody, objectMapper.getTypeFactory().constructParametricType(APIResponse.class, String.class));

        assertEquals("User registered successfully", response.getMessage());
        verify(authService).registerUser(validAuthDTO.getEmail(), validAuthDTO.getPassword());
    }

    @Test
    void register_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        AuthDTO invalidEmailDTO = new AuthDTO();
        invalidEmailDTO.setEmail("invalid-email");
        invalidEmailDTO.setPassword("password123");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void register_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        AuthDTO shortPasswordDTO = new AuthDTO();
        shortPasswordDTO.setEmail("test@example.com");
        shortPasswordDTO.setPassword("123");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortPasswordDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void register_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        doThrow(new BadCredentialsException("User already exists"))
                .when(authService)
                .registerUser(validAuthDTO.getEmail(), validAuthDTO.getPassword());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        APIResponse<String> response = objectMapper.readValue(
                responseBody, objectMapper.getTypeFactory().constructParametricType(APIResponse.class, String.class));

        assertEquals("Registration failed: User already exists", response.getMessage());
    }

    @Test
    void register_WithEmptyFields_ShouldReturnBadRequest() throws Exception {
        AuthDTO emptyDTO = new AuthDTO();
        emptyDTO.setEmail("");
        emptyDTO.setPassword("");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // Login Tests
    @Test
    void login_WithValidCredentials_ShouldReturnOk() throws Exception {
        when(authService.loginUser(validAuthDTO.getEmail(), validAuthDTO.getPassword()))
                .thenReturn(validAuthResponse);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.cookie().exists("refreshToken"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        APIResponse<AuthResponse> response = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructParametricType(APIResponse.class, AuthResponse.class));

        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(validAuthResponse.getEmail(), response.getData().getEmail());

        // Check refresh token cookie
        Cookie refreshCookie = result.getResponse().getCookie("refreshToken");
        assertNotNull(refreshCookie);
        assertEquals(validAuthResponse.getRefreshToken(), refreshCookie.getValue());

        verify(authService).loginUser(validAuthDTO.getEmail(), validAuthDTO.getPassword());
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        when(authService.loginUser(validAuthDTO.getEmail(), validAuthDTO.getPassword()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthDTO)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        APIResponse<String> response = objectMapper.readValue(
                responseBody, objectMapper.getTypeFactory().constructParametricType(APIResponse.class, String.class));

        assertEquals("Invalid email or password", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void login_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        AuthDTO invalidEmailDTO = new AuthDTO();
        invalidEmailDTO.setEmail("invalid-email");
        invalidEmailDTO.setPassword("password123");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    // Refresh Token Tests
    @Test
    void refreshToken_WithValidCookie_ShouldReturnOk() throws Exception {
        when(authService.refreshToken("valid-refresh-token")).thenReturn(validAuthResponse);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refreshToken", "valid-refresh-token")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.cookie().exists("refreshToken"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        APIResponse<AuthResponse> response = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructParametricType(APIResponse.class, AuthResponse.class));

        assertEquals("Token refreshed successfully", response.getMessage());
        assertNotNull(response.getData());
        verify(authService).refreshToken("valid-refresh-token");
    }

    @Test
    void refreshToken_WithMissingCookie_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        when(authService.refreshToken("invalid-token")).thenThrow(new BadCredentialsException("Invalid refresh token"));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refreshToken", "invalid-token")))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        APIResponse<String> response = objectMapper.readValue(
                responseBody, objectMapper.getTypeFactory().constructParametricType(APIResponse.class, String.class));

        assertEquals("Invalid refresh token", response.getMessage());
        assertNull(response.getData());
    }

    // Logout Tests
    @Test
    @WithMockUser(username = "test@example.com")
    void logout_WithValidPrincipal_ShouldReturnOk() throws Exception {
        doNothing().when(authService).logoutUser("test@example.com");

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/auth/logout").principal(() -> "test@example.com"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.cookie().maxAge("refreshToken", 0))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        APIResponse<String> response = objectMapper.readValue(
                responseBody, objectMapper.getTypeFactory().constructParametricType(APIResponse.class, String.class));

        assertEquals("Logged out successfully", response.getMessage());
        assertNull(response.getData());
        verify(authService).logoutUser("test@example.com");
    }

    // Forgot Password Tests
    @Test
    void forgotPassword_WithValidEmail_ShouldReturnOk() throws Exception {
        doNothing().when(authService).sendResetCodeByMail("test@example.com");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/forgot-password")
                        .param("email", "test@example.com"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        APIResponse<String> response = objectMapper.readValue(
                responseBody, objectMapper.getTypeFactory().constructParametricType(APIResponse.class, String.class));

        assertEquals("Reset code sent to your email if the account exists.", response.getMessage());
        assertNull(response.getData());
        verify(authService).sendResetCodeByMail("test@example.com");
    }

    @Test
    void forgotPassword_WithException_ShouldReturnBadRequest() throws Exception {
        doThrow(new BadCredentialsException("User not found"))
                .when(authService)
                .sendResetCodeByMail("nonexistent@example.com");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/forgot-password")
                        .param("email", "nonexistent@example.com"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        APIResponse<String> response = objectMapper.readValue(
                responseBody, objectMapper.getTypeFactory().constructParametricType(APIResponse.class, String.class));

        assertEquals("User not found", response.getMessage());
        assertNull(response.getData());
    }

    // Reset Password Tests
    @Test
    void resetPassword_WithValidParameters_ShouldReturnOk() throws Exception {
        doNothing().when(authService).verifyCodeAndResetPassword("test@example.com", "123456", "newPassword123");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/reset-password")
                        .param("email", "test@example.com")
                        .param("code", "123456")
                        .param("newPassword", "newPassword123"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        APIResponse<String> response = objectMapper.readValue(
                responseBody, objectMapper.getTypeFactory().constructParametricType(APIResponse.class, String.class));

        assertEquals("Password reset successfully.", response.getMessage());
        assertNull(response.getData());
        verify(authService).verifyCodeAndResetPassword("test@example.com", "123456", "newPassword123");
    }

    @Test
    void resetPassword_WithInvalidCode_ShouldReturnBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Invalid or expired code"))
                .when(authService)
                .verifyCodeAndResetPassword("test@example.com", "invalid", "newPassword123");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/reset-password")
                        .param("email", "test@example.com")
                        .param("code", "invalid")
                        .param("newPassword", "newPassword123"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        APIResponse<String> response = objectMapper.readValue(
                responseBody, objectMapper.getTypeFactory().constructParametricType(APIResponse.class, String.class));

        assertEquals("Invalid or expired code", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void resetPassword_WithMissingParameters_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/reset-password")
                        .param("email", "test@example.com"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
