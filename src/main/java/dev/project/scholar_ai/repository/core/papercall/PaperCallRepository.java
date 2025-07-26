package dev.project.scholar_ai.repository.core.papercall;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.project.scholar_ai.model.core.papercall.PaperCall;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaperCallRepository extends JpaRepository<PaperCall, UUID> {

    boolean existsByUserIdAndTitleAndLink(UUID userId, String title, String link);

    @Query("SELECT pc FROM PaperCall pc WHERE pc.userId = :userId " +
            "AND (:domain IS NULL OR pc.domain ILIKE %:domain%) " +
            "AND (:type IS NULL OR pc.type = :type) " +
            "AND (:source IS NULL OR pc.source = :source) " +
            "AND (:searchTerm IS NULL OR pc.title ILIKE %:searchTerm%) " +
            "AND (:deadlineFrom IS NULL OR pc.deadline >= :deadlineFrom) " +
            "AND (:deadlineTo IS NULL OR pc.deadline <= :deadlineTo)")
    Page<PaperCall> filter(
            @Param("userId") UUID userId,
            @Param("source") String source,
            @Param("type") String type,
            @Param("domain") String domain,
            @Param("searchTerm") String searchTerm,
            @Param("deadlineFrom") LocalDate deadlineFrom,
            @Param("deadlineTo") LocalDate deadlineTo,
            Pageable pageable
    );

    @Query("SELECT pc FROM PaperCall pc WHERE pc.userId = :userId AND LOWER(pc.domain) LIKE LOWER(CONCAT('%', :domain, '%'))")
    List<PaperCall> findByUserIdAndDomain(@Param("userId") UUID userId, @Param("domain") String domain);

    @Query("SELECT COUNT(pc) FROM PaperCall pc WHERE pc.userId = :userId AND pc.domain LIKE %:domain%")
    long countByUserIdAndDomain(@Param("userId") UUID userId, @Param("domain") String domain);

    @Query("SELECT pc.type, COUNT(pc) FROM PaperCall pc WHERE pc.userId = :userId AND pc.domain LIKE %:domain% GROUP BY pc.type")
    List<Object[]> countByTypeForUser(@Param("userId") UUID userId, @Param("domain") String domain);

    @Query("SELECT pc.source, COUNT(pc) FROM PaperCall pc WHERE pc.userId = :userId AND pc.domain LIKE %:domain% GROUP BY pc.source")
    List<Object[]> countBySourceForUser(@Param("userId") UUID userId, @Param("domain") String domain);

    void deleteByUserIdAndDomain(UUID userId, String domain);
}
