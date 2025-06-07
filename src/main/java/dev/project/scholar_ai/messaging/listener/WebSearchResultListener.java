package dev.project.scholar_ai.messaging.listener;

import dev.project.scholar_ai.dto.event.WebSearchCompletedEvent;
import dev.project.scholar_ai.service.PaperPersistenceService;
import dev.project.scholar_ai.service.WebSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSearchResultListener {

    private final WebSearchService webSearchService;
    private final PaperPersistenceService paperPersistenceService;

    @RabbitListener(queues = "${scholarai.rabbitmq.web-search.completed-queue}", containerFactory = "listenerFactory")
    @Transactional(transactionManager = "paperTransactionManager")
    public void onWebSearchCompleted(WebSearchCompletedEvent event) {
        log.info(
                "📄 Received web search result - Project ID: {}, Correlation ID: {}, Papers found: {}",
                event.projectId(),
                event.correlationId(),
                event.papers().size());

        try {
            // Update the search results in the service
            webSearchService.updateSearchResults(event);

            // Persist papers to database
            paperPersistenceService.savePapers(event.papers(), event.projectId());

            // Log summary of processed papers
            event.papers().forEach(paper -> {
                log.debug(
                        "📝 Processed paper: '{}' | DOI: {} | Citations: {} | Source: {}",
                        paper.title(),
                        paper.doi(),
                        paper.citationCount(),
                        paper.source());
            });

            log.info(
                    "✅ Web search processing completed successfully for correlation ID: {} - {} papers persisted",
                    event.correlationId(),
                    event.papers().size());

        } catch (Exception e) {
            log.error(
                    "❌ Failed to process web search results for correlation ID: {} - Error: {}",
                    event.correlationId(),
                    e.getMessage(),
                    e);
            throw new RuntimeException("Failed to process web search results", e);
        }
    }
}
