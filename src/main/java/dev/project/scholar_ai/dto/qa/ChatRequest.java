package dev.project.scholar_ai.dto.qa;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {
    private String message;
    private UUID sessionId; // Optional: for continuing existing conversation
    private String sessionTitle; // Optional: for new session
}
