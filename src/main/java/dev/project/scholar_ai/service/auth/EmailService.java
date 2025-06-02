package dev.project.scholar_ai.service.auth;

public class EmailService {
    public void sendResetCode(String to, String code) {
        System.out.printf("Sending reset code to %s: %s%n", to, code);
        // You can integrate SendGrid, Mailgun, SMTP, or SMS here
    }
}
