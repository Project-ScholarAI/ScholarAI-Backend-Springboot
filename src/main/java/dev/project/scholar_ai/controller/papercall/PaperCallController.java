package dev.project.scholar_ai.controller.papercall;

import dev.project.scholar_ai.dto.papercall.PaperCallResponse;
import dev.project.scholar_ai.dto.papercall.PaperCallStatisticsResponse;
import dev.project.scholar_ai.service.papercall.PaperCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/papercall")
@RequiredArgsConstructor
@Slf4j
public class PaperCallController {

    private final PaperCallService paperCallService;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncFromFastAPI(
            @RequestParam String domain,
            Authentication authentication) {
        try {
            log.info("paper call sync endpoint hitted");
            UUID userId = getUserIdFromAuthentication(authentication);
            paperCallService.syncCallsFromFastAPI(userId, domain);
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Successfully synced calls from FastAPI.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error syncing paper calls", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
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
            Authentication authentication) {

        try {
            log.info("Paper call filter endpoint hitted");
            UUID userId = getUserIdFromAuthentication(authentication);
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
            Authentication authentication) {
        try {
            log.info("Paper call statistics endpoint hitted");
            UUID userId = getUserIdFromAuthentication(authentication);
            PaperCallStatisticsResponse stats = paperCallService.getStatistics(userId, domain);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        // Replace this with actual logic to extract user ID from authentication
        if (authentication != null && authentication.isAuthenticated()) {
            return UUID.randomUUID();
        }
        throw new RuntimeException("User not authenticated");
    }
}
