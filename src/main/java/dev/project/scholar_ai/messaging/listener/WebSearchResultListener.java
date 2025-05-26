package dev.project.scholar_ai.messaging.listener;

import dev.project.scholar_ai.dto.event.WebSearchCompletedEvent;
import dev.project.scholar_ai.dto.event.EnhancedPaperMetadata;
import dev.project.scholar_ai.service.WebSearchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebSearchResultListener {

    private final WebSearchService webSearchService;
    
    public WebSearchResultListener(WebSearchService webSearchService) {
        this.webSearchService = webSearchService;
    }

    @RabbitListener(queues = "${scholarai.rabbitmq.web-search.completed-queue}", containerFactory = "listenerFactory")
    @Transactional
    public void onWebSearchCompleted(WebSearchCompletedEvent evt) {
        System.out.println("📄 Received web search result for project ID: " + evt.projectId());
        System.out.println("🔗 Correlation ID: " + evt.correlationId());
        System.out.println("📚 Papers found: " + evt.papers().size());
        
        // Update the search results in the service
        webSearchService.updateSearchResults(evt);
        
        // Log paper details
        for (EnhancedPaperMetadata paper : evt.papers()) {
            System.out.println("📝 Processing paper: " + paper.title());
            System.out.println("🔗 DOI: " + paper.doi());
            System.out.println("📅 Publication Date: " + paper.publicationDate());
            System.out.println("🏛️ Venue: " + paper.venueName());
            System.out.println("📊 Citations: " + paper.citationCount());
            System.out.println("📄 Paper URL: " + paper.paperUrl());
            System.out.println("📥 PDF URL: " + paper.pdfUrl());
            
            // TODO: Implement actual database persistence
            // paperRepository.upsertByDoi(paper.doi(), paper.title(), paper.authors(), 
            //                           paper.pdfContent(), paper.paperUrl(), etc.);
        }
        
        System.out.println("✅ Web search processing completed successfully!");
        System.out.println("🎯 Results are now available via API endpoints!");
        
        // 2) Optionally trigger next step in pipeline (e.g., content extraction)
        // if any papers have PDF content available
    }
} 