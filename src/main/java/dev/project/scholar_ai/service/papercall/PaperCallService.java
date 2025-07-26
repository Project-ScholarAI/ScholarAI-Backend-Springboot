package dev.project.scholar_ai.service.papercall;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.project.scholar_ai.dto.papercall.PaperCallResponse;
import dev.project.scholar_ai.dto.papercall.PaperCallStatisticsResponse;
import dev.project.scholar_ai.model.core.papercall.PaperCall;
import dev.project.scholar_ai.repository.core.papercall.PaperCallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaperCallService {

    private final PaperCallRepository paperCallRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${external.papercall.api.base-url}")
    private String baseApiUrl;

    public List<PaperCallResponse> syncCallsFromFastAPI(UUID userId, String domain) {
        String url = baseApiUrl + "/calls?domain=" + domain;
        log.info("🔄 Sync is called in paper-call-service: {}", url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        try {
            List<PaperCallResponse> calls = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            List<PaperCall> newEntities = new ArrayList<>();

            for (PaperCallResponse dto : calls) {
                boolean exists = paperCallRepository.existsByUserIdAndTitleAndLink(userId, dto.getTitle(), dto.getLink());
                if (!exists) {
                    PaperCall entity = PaperCall.builder()
                            .userId(userId)
                            .title(dto.getTitle())
                            .link(dto.getLink())
                            .type(dto.getType())
                            .source(dto.getSource())
                            .domain(domain)
                            .whenHeld(dto.getWhenHeld())
                            .whereHeld(dto.getWhereHeld())
                            .deadline(dto.getDeadline())
                            .description(dto.getDescription())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    newEntities.add(entity);
                }
            }

            if (!newEntities.isEmpty()) {
                paperCallRepository.saveAll(newEntities);
                log.info("✅ Synced {} new paper calls for user {}", newEntities.size(), userId);
            } else {
                log.info("ℹ️ No new calls to sync for user {}", userId);
            }

            // ✅ Return ALL items (not just newly added)
            List<PaperCall> allCalls = paperCallRepository.findByUserIdAndDomain(userId, domain);
            log.info("here returned sync paper calls ,", calls.size());
            log.info(calls.toString());
            return allCalls.stream().map(this::mapToDto).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Error parsing or saving paper calls", e);
            throw new RuntimeException("Failed to sync paper calls", e);
        }
    }


    public Page<PaperCallResponse> filterPaperCalls(UUID userId, String source, String type, String domain,
                                                    String searchTerm, LocalDate deadlineFrom, LocalDate deadlineTo,
                                                    int page, int size, String sortBy, String sortDir) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        Page<PaperCall> pageData = paperCallRepository.filter(userId, source, type, domain, searchTerm,
                deadlineFrom, deadlineTo, pageable);

        return pageData.map(this::mapToDto);
    }

    public PaperCallStatisticsResponse getStatistics(UUID userId, String domain) {
        List<PaperCall> calls = paperCallRepository.findByUserIdAndDomain(userId, domain);

        Map<String, Long> sourceCount = calls.stream()
                .filter(call -> call.getSource() != null)
                .collect(Collectors.groupingBy(PaperCall::getSource, Collectors.counting()));

        long conferences = calls.stream()
                .filter(c -> "conference".equalsIgnoreCase(c.getType()))
                .count();

        long journals = calls.stream()
                .filter(c -> "journal".equalsIgnoreCase(c.getType()))
                .count();

        return PaperCallStatisticsResponse.builder()
                .domain(domain)
                .totalCalls(calls.size())
                .conferences((int) conferences)
                .journals((int) journals)
                .sources(sourceCount.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue())))
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public List<PaperCallResponse> getAllCallsByUser(UUID userId) {
        log.info("get all for paper call hitted in service for userId", userId);
        List<PaperCall> calls = paperCallRepository.findByUserId(userId);
        log.info("all calls size", calls.size());
        log.info("calls:", calls);
        return calls.stream().map(this::mapToDto).collect(Collectors.toList());
    }



    private PaperCallResponse mapToDto(PaperCall call) {
        return PaperCallResponse.builder()
                .title(call.getTitle())
                .link(call.getLink())
                .type(call.getType())
                .source(call.getSource())
                .whenHeld(call.getWhenHeld())
                .whereHeld(call.getWhereHeld())
                .deadline(call.getDeadline())
                .description(call.getDescription())
                .build();
    }
}
