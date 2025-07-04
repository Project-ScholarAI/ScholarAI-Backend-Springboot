package dev.project.scholar_ai.repository.qa;

import dev.project.scholar_ai.model.qa.QAMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QAMessageRepository extends JpaRepository<QAMessage, UUID> {
    
    /**
     * Find messages by session ID ordered by creation time
     */
    List<QAMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
    
    /**
     * Find messages by session ID with pagination
     */
    Page<QAMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId, Pageable pageable);
    
    /**
     * Find recent messages by session ID (for conversation context)
     */
    @Query("SELECT m FROM QAMessage m WHERE m.session.id = :sessionId ORDER BY m.createdAt DESC")
    List<QAMessage> findRecentMessagesBySessionId(@Param("sessionId") UUID sessionId, Pageable pageable);
    
    /**
     * Count messages by session ID
     */
    long countBySessionId(UUID sessionId);
    
    /**
     * Delete messages by session ID
     */
    void deleteBySessionId(UUID sessionId);
}