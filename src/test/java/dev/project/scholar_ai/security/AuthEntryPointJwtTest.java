package dev.project.scholar_ai.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

    @InjectMocks
    private AuthEntryPointJwt authEntryPointJwt;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void commence_shouldSetUnauthorizedResponse() throws IOException {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException =
                new AuthenticationException("Full authentication is required to access this resource") {};

        when(request.getServletPath()).thenReturn("/api/test/secure");

        // When
        authEntryPointJwt.commence(request, response, authException);

        // Then
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());

        Map<String, Object> body = objectMapper.readValue(response.getContentAsString(), Map.class);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, body.get("status"));
        assertEquals("Unauthorized", body.get("error"));
        assertEquals("Full authentication is required to access this resource", body.get("message"));
        assertEquals("/api/test/secure", body.get("path"));
    }
}
