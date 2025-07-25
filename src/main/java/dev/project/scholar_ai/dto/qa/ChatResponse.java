package dev.project.scholar_ai.dto.qa;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private UUID sessionId;
    private String response;
    private LocalDateTime timestamp;
    private boolean success;
    private String error;

    // Constructor for error responses
    public ChatResponse(String error) {
        this.error = error;
        this.success = false;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for successful responses
    public ChatResponse(UUID sessionId, String response) {
        this.sessionId = sessionId;
        this.response = response;
        this.success = true;
        this.timestamp = LocalDateTime.now();
    }
}
