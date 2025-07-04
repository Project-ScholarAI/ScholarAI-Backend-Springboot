package dev.project.scholar_ai.service.auth;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${spring.sendgrid.from-email}")
    private String fromEmail;

    @Value("${spring.sendgrid.template-id}")
    private String templateId;

    private SendGrid sendGrid;

    @PostConstruct
    public void init() {
        this.sendGrid = new SendGrid(sendGridApiKey);
    }

    public void sendResetCodeByEmail(String to, String code) {
        if (to == null || code == null) {
            throw new IllegalArgumentException("Email and code cannot be null");
        }

        System.out.printf("Sending reset code to %s: %s%n", to, code);
        Email from = new Email(fromEmail);
        Email toEmail = new Email(to);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(templateId);

        Personalization personalization = new Personalization();
        personalization.addTo(toEmail);

        try {
            personalization.addDynamicTemplateData("RESET_CODE", code);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add template data", e);
        }

        mail.addPersonalization(personalization);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("/mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid error: " + response.getBody());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
