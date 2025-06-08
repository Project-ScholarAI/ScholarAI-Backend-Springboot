package dev.project.scholar_ai.service.websearch;

import dev.project.scholar_ai.dto.agent.request.WebSearchRequest;
import dev.project.scholar_ai.dto.agent.request.WebSearchRequestDTO;
import dev.project.scholar_ai.dto.agent.response.WebSearchResponseDto;
import dev.project.scholar_ai.dto.event.WebSearchCompletedEvent;
import dev.project.scholar_ai.dto.paper.metadata.PaperMetadataDto;
import dev.project.scholar_ai.messaging.publisher.WebSearchRequestSender;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSearchService {

    private final WebSearchRequestSender webSearchRequestSender;
    private final Map<String, WebSearchResponseDto> searchResults = new ConcurrentHashMap<>();

    public WebSearchResponseDto initiateWebSearch(WebSearchRequestDTO requestDto) {
        UUID projectId = UUID.randomUUID();
        String correlationId = UUID.randomUUID().toString();

        log.info(
                "Initiating web search - Project ID: {}, Correlation ID: {}, Query: {}, Domain: {}, Batch Size: {}",
                projectId,
                correlationId,
                requestDto.queryTerms(),
                requestDto.domain(),
                requestDto.batchSize());

        // Create and send the web search request
        WebSearchRequest webSearchRequest = new WebSearchRequest(
                projectId, requestDto.queryTerms(), requestDto.domain(), requestDto.batchSize(), correlationId);

        webSearchRequestSender.send(webSearchRequest);

        // Create response DTO
        WebSearchResponseDto response = new WebSearchResponseDto(
                projectId.toString(),
                correlationId,
                requestDto.queryTerms(),
                requestDto.domain(),
                requestDto.batchSize(),
                "SUBMITTED",
                LocalDateTime.now(),
                "Web search job submitted successfully. Results will be available shortly.",
                List.of() // Empty initially
                );

        // Store the response for later updates
        searchResults.put(correlationId, response);

        return response;
    }

    public void updateSearchResults(WebSearchCompletedEvent event) {
        String correlationId = event.correlationId();
        WebSearchResponseDto existingResponse = searchResults.get(correlationId);

        log.debug("Updating search results for correlation ID: {}", correlationId);

        if (existingResponse != null) {
            // Use papers directly without conversion
            List<PaperMetadataDto> papers = event.papers();

            // Update the response with papers
            WebSearchResponseDto updatedResponse = new WebSearchResponseDto(
                    existingResponse.projectId(),
                    existingResponse.correlationId(),
                    existingResponse.queryTerms(),
                    existingResponse.domain(),
                    existingResponse.batchSize(),
                    "COMPLETED",
                    existingResponse.submittedAt(),
                    String.format("Web search completed successfully! Found %d papers.", papers.size()),
                    papers);

            searchResults.put(correlationId, updatedResponse);
            log.info("Updated search results for correlation ID: {} with {} papers", correlationId, papers.size());
        } else {
            log.warn("No existing search response found for correlation ID: {}", correlationId);
        }
    }

    public WebSearchResponseDto getSearchResults(String correlationId) {
        return searchResults.get(correlationId);
    }

    public List<WebSearchResponseDto> getAllSearchResults() {
        return List.copyOf(searchResults.values());
    }
}
