package dev.project.scholar_ai.repository.qa;

import dev.project.scholar_ai.model.qa.QASession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QASessionRepository extends JpaRepository<QASession, UUID> {

    /**
     * Find sessions by user ID
     */
    Page<QASession> findByUserIdOrderByUpdatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find sessions by user ID and project ID
     */
    Page<QASession> findByUserIdAndProjectIdOrderByUpdatedAtDesc(UUID userId, UUID projectId, Pageable pageable);

    /**
     * Find session by ID and user ID (for authorization)
     */
    Optional<QASession> findByIdAndUserId(UUID sessionId, UUID userId);

    /**
     * Find sessions by project ID
     */
    List<QASession> findByProjectIdOrderByUpdatedAtDesc(UUID projectId);

    /**
     * Count sessions by user ID
     */
    long countByUserId(UUID userId);
}
