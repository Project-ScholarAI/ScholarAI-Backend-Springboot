package dev.project.scholar_ai.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sendgrid.*;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private SendGrid sendGrid;

    @InjectMocks
    private EmailService emailService;

    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_FROM_EMAIL = "from@example.com";
    private static final String TEST_TEMPLATE_ID = "template-123";
    private static final String TEST_TO_EMAIL = "to@example.com";
    private static final String TEST_RESET_CODE = "123456";

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(emailService, "sendGridApiKey", TEST_API_KEY);
        ReflectionTestUtils.setField(emailService, "fromEmail", TEST_FROM_EMAIL);
        ReflectionTestUtils.setField(emailService, "templateId", TEST_TEMPLATE_ID);
        ReflectionTestUtils.setField(emailService, "sendGrid", sendGrid);
    }

    @Test
    void sendResetCodeByEmail_Success() throws Exception {
        // Given
        Response successResponse = new Response();
        successResponse.setStatusCode(202);
        when(sendGrid.api(any(Request.class))).thenReturn(successResponse);

        // When
        emailService.sendResetCodeByEmail(TEST_TO_EMAIL, TEST_RESET_CODE);

        // Then
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid).api(requestCaptor.capture());

        Request capturedRequest = requestCaptor.getValue();
        assertEquals(Method.POST, capturedRequest.getMethod());
        assertEquals("/mail/send", capturedRequest.getEndpoint());

        String requestBody = capturedRequest.getBody();
        assertTrue(requestBody.contains(TEST_FROM_EMAIL));
        assertTrue(requestBody.contains(TEST_TO_EMAIL));
        assertTrue(requestBody.contains(TEST_TEMPLATE_ID));
        assertTrue(requestBody.contains(TEST_RESET_CODE));
    }

    @Test
    void sendResetCodeByEmail_SendGridError() throws Exception {
        // Given
        Response errorResponse = new Response();
        errorResponse.setStatusCode(400);
        errorResponse.setBody("Bad Request");
        when(sendGrid.api(any(Request.class))).thenReturn(errorResponse);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> emailService.sendResetCodeByEmail(TEST_TO_EMAIL, TEST_RESET_CODE));
        assertTrue(exception.getMessage().contains("SendGrid error"));
    }

    @Test
    void sendResetCodeByEmail_IOException() throws Exception {
        // Given
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("Network error"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> emailService.sendResetCodeByEmail(TEST_TO_EMAIL, TEST_RESET_CODE));
        assertEquals("Failed to send email", exception.getMessage());
    }

    @Test
    void sendResetCodeByEmail_NullEmail() {
        assertThrows(IllegalArgumentException.class, () -> emailService.sendResetCodeByEmail(null, TEST_RESET_CODE));
    }

    @Test
    void sendResetCodeByEmail_NullCode() {
        assertThrows(IllegalArgumentException.class, () -> emailService.sendResetCodeByEmail(TEST_TO_EMAIL, null));
    }
}
