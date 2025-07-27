package dev.project.scholar_ai.controller.papercall;

import dev.project.scholar_ai.dto.papercall.PaperCallResponse;
import dev.project.scholar_ai.dto.papercall.PaperCallStatisticsResponse;
import dev.project.scholar_ai.model.core.auth.AuthUser;
import dev.project.scholar_ai.repository.core.auth.AuthUserRepository;
import dev.project.scholar_ai.service.papercall.PaperCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/papercall")
@RequiredArgsConstructor
@Slf4j
public class PaperCallController {

    private final PaperCallService paperCallService;
    private final AuthUserRepository authUserRepository;

    @PostMapping("/sync")
    public ResponseEntity<List<PaperCallResponse>> syncFromFastAPI(
            @RequestParam String domain,
            Principal principal) {
        try {
            log.info("📥 paper call sync endpoint hit with domain '{}'", domain);

            UUID userId = getUserIdFromPrincipal(principal);
            List<PaperCallResponse> syncedCalls = paperCallService.syncCallsFromFastAPI(userId, domain);

            return ResponseEntity.ok(syncedCalls);

        } catch (Exception e) {
            log.error("❌ Error syncing paper calls", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList()); // You can also return an error object if needed
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<PaperCallResponse>> filterCalls(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Principal principal) {

        try {
            log.info("Paper call filter endpoint hitted");
            UUID userId = getUserIdFromPrincipal(principal);
            Page<PaperCallResponse> result = paperCallService.filterPaperCalls(
                    userId, source, type, domain, searchTerm, deadlineFrom, deadlineTo, page, size, sortBy, sortDir);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error filtering paper calls", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<PaperCallStatisticsResponse> getStatistics(
            @RequestParam String domain,
            Principal principal) {
        try {
            log.info("Paper call statistics endpoint hitted");
            UUID userId = getUserIdFromPrincipal(principal);
            PaperCallStatisticsResponse stats = paperCallService.getStatistics(userId, domain);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/all")
    public ResponseEntity<List<PaperCallResponse>> getAllCallsByUser(Principal principal) {
        try {
            log.info("paper call all endpoint hit");
            log.info("Principal: {}", principal);
            log.info("Principal name: {}", principal != null ? principal.getName() : "null");

            UUID userId = getUserIdFromPrincipal(principal);
            log.info("User ID from all endpoint: {}", userId);

            List<PaperCallResponse> calls = paperCallService.getAllCallsByUser(userId);
            log.info("✅ All call size in endpoint: {}", calls.size());
            return ResponseEntity.ok(calls);

        } catch (Exception e) {
            log.error("❌ Error fetching all paper calls by user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "healthy");
        info.put("service", "PaperCall");
        info.put("version", "1.0.0");
        info.put("sources", new String[]{"WikiCFP", "MDPI", "Taylor & Francis", "Springer"});
        info.put("endpoints", new String[]{"/sync", "/filter", "/statistics"});
        return ResponseEntity.ok(info);
    }

    private UUID getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Authentication required");
        }
        String email = principal.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Invalid authentication token");
        }
        AuthUser user = authUserRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        return user.getId();
    }
}
