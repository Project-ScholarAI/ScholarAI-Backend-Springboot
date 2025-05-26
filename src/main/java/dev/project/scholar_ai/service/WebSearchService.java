package dev.project.scholar_ai.service;

import dev.project.scholar_ai.dto.common.AuthorDetailsDto;
import dev.project.scholar_ai.dto.common.PaperDetailsDto;
import dev.project.scholar_ai.dto.common.WebSearchRequestDto;
import dev.project.scholar_ai.dto.common.WebSearchResponseDto;
import dev.project.scholar_ai.dto.agentRequests.WebSearchRequest;
import dev.project.scholar_ai.dto.event.AuthorInfo;
import dev.project.scholar_ai.dto.event.EnhancedPaperMetadata;
import dev.project.scholar_ai.dto.event.WebSearchCompletedEvent;
import dev.project.scholar_ai.messaging.publisher.WebSearchRequestSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WebSearchService {
    
    private final WebSearchRequestSender webSearchRequestSender;
    private final Map<String, WebSearchResponseDto> searchResults = new ConcurrentHashMap<>();
    
    public WebSearchService(WebSearchRequestSender webSearchRequestSender) {
        this.webSearchRequestSender = webSearchRequestSender;
    }
    
    public WebSearchResponseDto initiateWebSearch(WebSearchRequestDto requestDto) {
        UUID projectId = UUID.randomUUID();
        String correlationId = UUID.randomUUID().toString();
        
        // Create and send the web search request
        WebSearchRequest webSearchRequest = new WebSearchRequest(
            projectId, 
            requestDto.queryTerms(), 
            requestDto.domain(), 
            requestDto.batchSize(), 
            correlationId
        );
        
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
        
        if (existingResponse != null) {
            // Convert papers to DTOs
            List<PaperDetailsDto> paperDtos = event.papers().stream()
                .map(this::convertToPaperDto)
                .collect(Collectors.toList());
            
            // Update the response with papers
            WebSearchResponseDto updatedResponse = new WebSearchResponseDto(
                existingResponse.projectId(),
                existingResponse.correlationId(),
                existingResponse.queryTerms(),
                existingResponse.domain(),
                existingResponse.batchSize(),
                "COMPLETED",
                existingResponse.submittedAt(),
                String.format("Web search completed successfully! Found %d papers.", paperDtos.size()),
                paperDtos
            );
            
            searchResults.put(correlationId, updatedResponse);
        }
    }
    
    public WebSearchResponseDto getSearchResults(String correlationId) {
        return searchResults.get(correlationId);
    }
    
    public List<WebSearchResponseDto> getAllSearchResults() {
        return List.copyOf(searchResults.values());
    }
    
    private PaperDetailsDto convertToPaperDto(EnhancedPaperMetadata paper) {
        List<AuthorDetailsDto> authorDtos = paper.authors() != null ? 
            paper.authors().stream()
                .map(this::convertToAuthorDto)
                .collect(Collectors.toList()) : 
            List.of();
        
        return new PaperDetailsDto(
            paper.title(),
            paper.doi(),
            paper.publicationDate(),
            paper.venueName(),
            paper.publisher(),
            paper.peerReviewed(),
            authorDtos,
            paper.citationCount(),
            paper.codeRepositoryUrl(),
            paper.datasetUrl(),
            paper.paperUrl(),
            paper.pdfUrl(), // PDF download URL
            "Multi-Source", // Default source
            null // Abstract not available in current structure
        );
    }
    
    private AuthorDetailsDto convertToAuthorDto(AuthorInfo author) {
        return new AuthorDetailsDto(
            author.name(),
            null, // Affiliation not available in current structure
            null  // Email not available in current structure
        );
    }
} 