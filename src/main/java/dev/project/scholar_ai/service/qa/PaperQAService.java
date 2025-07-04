package dev.project.scholar_ai.service.qa;

import dev.project.scholar_ai.dto.qa.ChatRequest;
import dev.project.scholar_ai.dto.qa.ChatResponse;
import dev.project.scholar_ai.enums.MessageRole;
import dev.project.scholar_ai.model.core.auth.AuthUser;
import dev.project.scholar_ai.model.paper.metadata.Paper;
import dev.project.scholar_ai.model.qa.QAMessage;
import dev.project.scholar_ai.model.qa.QASession;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.repository.paper.PaperRepository;
import dev.project.scholar_ai.repository.qa.QAMessageRepository;
import dev.project.scholar_ai.repository.qa.QASessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaperQAService {
    
    private final QASessionRepository qaSessionRepository;
    private final QAMessageRepository qaMessageRepository;
    private final PaperRepository paperRepository;
    private final AuthUserRepository authUserRepository;
    private final RestTemplate restTemplate;
    
    @Value("${scholarai.fastapi.base-url:http://localhost:8000}")
    private String fastApiBaseUrl;
    
    /**
     * Chat with a paper using extracted text and structured content
     */
    public ChatResponse chatWithPaper(UUID paperId, ChatRequest request, String userEmail) {
        try {
            log.info("Processing chat request for paper: {} by user: {}", paperId, userEmail);
            
            // Get user
            AuthUser user = authUserRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
            
            // Get paper
            Paper paper = paperRepository.findById(paperId)
                    .orElseThrow(() -> new RuntimeException("Paper not found: " + paperId));
            
            // Check if paper has extracted text
            if (paper.getExtractedText() == null || paper.getExtractedText().trim().isEmpty()) {
                return new ChatResponse("Paper has no extracted text. Please extract text first.");
            }
            
            // Get or create session
            QASession session = getOrCreateSession(request, user, paper);
            
            // Save user message
            QAMessage userMessage = QAMessage.builder()
                    .session(session)
                    .role(MessageRole.USER)
                    .content(request.getMessage())
                    .metadata(new HashMap<>())
                    .build();
            qaMessageRepository.save(userMessage);
            
            // Get conversation history
            List<QAMessage> recentMessages = qaMessageRepository.findRecentMessagesBySessionId(
                    session.getId(), PageRequest.of(0, 10));
            
            // Call FastAPI QA service
            String response = callFastAPIQAService(paper, recentMessages, request.getMessage());
            
            // Save assistant response
            QAMessage assistantMessage = QAMessage.builder()
                    .session(session)
                    .role(MessageRole.ASSISTANT)
                    .content(response)
                    .metadata(new HashMap<>())
                    .build();
            qaMessageRepository.save(assistantMessage);
            
            return new ChatResponse(session.getId(), response);
            
        } catch (Exception e) {
            log.error("Failed to process chat request for paper {}: {}", paperId, e.getMessage(), e);
            return new ChatResponse("Error processing your question: " + e.getMessage());
        }
    }
    
    /**
     * Get or create a QA session
     */
    private QASession getOrCreateSession(ChatRequest request, AuthUser user, Paper paper) {
        if (request.getSessionId() != null) {
            return qaSessionRepository.findByIdAndUserId(request.getSessionId(), user.getId())
                    .orElseThrow(() -> new RuntimeException("Session not found or unauthorized"));
        }
        
        // Create new session
        String sessionTitle = request.getSessionTitle() != null ? 
                request.getSessionTitle() : 
                "Chat with: " + (paper.getTitle() != null ? paper.getTitle() : "Paper");
        
        QASession session = QASession.builder()
                .user(user)
                .title(sessionTitle)
                .build();
        
        return qaSessionRepository.save(session);
    }
    
    /**
     * Call FastAPI QA service
     */
    private String callFastAPIQAService(Paper paper, List<QAMessage> conversationHistory, String query) {
        try {
            String url = fastApiBaseUrl + "/papers/" + paper.getId() + "/chat";
            
            // Build request payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("query", query);
            payload.put("paper_content", paper.getExtractedText());
            payload.put("paper_metadata", buildPaperMetadata(paper));
            payload.put("conversation_history", formatConversationHistory(conversationHistory));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            
            // Make API call
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            
            if (response != null && response.containsKey("response")) {
                return (String) response.get("response");
            } else {
                return "Sorry, I couldn't process your question at the moment.";
            }
            
        } catch (Exception e) {
            log.error("Failed to call FastAPI QA service: {}", e.getMessage(), e);
            return "Sorry, there was an error processing your question.";
        }
    }
    
    /**
     * Build paper metadata for QA context
     */
    private Map<String, Object> buildPaperMetadata(Paper paper) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", paper.getTitle());
        metadata.put("authors", paper.getAuthors());
        metadata.put("abstract", paper.getAbstractText());
        metadata.put("source", paper.getSource());
        metadata.put("publication_date", paper.getPublicationDate());
        metadata.put("doi", paper.getDoi());
        return metadata;
    }
    
    /**
     * Format conversation history for FastAPI
     */
    private List<Map<String, Object>> formatConversationHistory(List<QAMessage> messages) {
        return messages.stream()
                .map(message -> {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("role", message.getRole().name().toLowerCase());
                    msg.put("content", message.getContent());
                    msg.put("timestamp", message.getCreatedAt());
                    return msg;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get conversation history for a session
     */
    public List<QAMessage> getConversationHistory(UUID sessionId, String userEmail) {
        // Verify user has access to this session
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        qaSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new RuntimeException("Session not found or unauthorized"));
        
        return qaMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
    
    /**
     * Get user's QA sessions
     */
    public List<QASession> getUserSessions(String userEmail) {
        AuthUser user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        return qaSessionRepository.findByUserIdOrderByUpdatedAtDesc(user.getId(), PageRequest.of(0, 50))
                .getContent();
    }
}