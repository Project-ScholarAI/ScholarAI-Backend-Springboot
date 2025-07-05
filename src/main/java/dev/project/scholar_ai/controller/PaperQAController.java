package dev.project.scholar_ai.controller;

import dev.project.scholar_ai.dto.qa.ChatRequest;
import dev.project.scholar_ai.dto.qa.ChatResponse;
import dev.project.scholar_ai.model.qa.QAMessage;
import dev.project.scholar_ai.model.qa.QASession;
import dev.project.scholar_ai.service.qa.PaperQAService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
public class PaperQAController {

    private final PaperQAService paperQAService;

    /**
     * Chat with a paper using extracted text and structured content
     */
    @PostMapping("/{paperId}/chat")
    public ResponseEntity<ChatResponse> chatWithPaper(
            @PathVariable UUID paperId, @RequestBody ChatRequest request, Authentication authentication) {

        try {
            log.info("Chat request received for paper: {} by user: {}", paperId, authentication.getName());

            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ChatResponse("Message cannot be empty"));
            }

            ChatResponse response = paperQAService.chatWithPaper(paperId, request, authentication.getName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in chat endpoint for paper {}: {}", paperId, e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(new ChatResponse("Error processing your question: " + e.getMessage()));
        }
    }

    /**
     * Get conversation history for a QA session
     */
    @GetMapping("/chat/sessions/{sessionId}/messages")
    public ResponseEntity<List<QAMessage>> getConversationHistory(
            @PathVariable UUID sessionId, Authentication authentication) {

        try {
            List<QAMessage> messages = paperQAService.getConversationHistory(sessionId, authentication.getName());

            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            log.error("Error getting conversation history for session {}: {}", sessionId, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get user's QA sessions
     */
    @GetMapping("/chat/sessions")
    public ResponseEntity<List<QASession>> getUserSessions(Authentication authentication) {

        try {
            List<QASession> sessions = paperQAService.getUserSessions(authentication.getName());
            return ResponseEntity.ok(sessions);

        } catch (Exception e) {
            log.error("Error getting user sessions for user {}: {}", authentication.getName(), e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Health check endpoint for QA functionality
     */
    @GetMapping("/chat/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("QA service is running");
    }
}
