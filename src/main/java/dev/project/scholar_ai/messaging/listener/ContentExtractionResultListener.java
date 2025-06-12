package dev.project.scholar_ai.messaging.listener;

import dev.project.scholar_ai.dto.event.ContentExtractionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentExtractionResultListener {

    @RabbitListener(
            queues = "scholarai.content-extraction.completed.queue",
            containerFactory = "listenerFactory")
    @Transactional(transactionManager = "paperTransactionManager")
    public void onContentExtractionCompleted(ContentExtractionCompletedEvent event) {
        log.info(
                "📄 Received content extraction result - Project ID: {}, Correlation ID: {}, Papers processed: {}",
                event.projectId(),
                event.correlationId(),
                event.papersProcessed());

        try {
            // TODO: Implement content extraction result processing
            // 1. Update ContentExtractionOperation status
            // 2. Save PaperContent entities
            // 3. Save PaperSummary entities
            // 4. Save Citation entities

            log.info(
                    "✅ Content extraction processing completed successfully for correlation ID: {} - {} papers processed",
                    event.correlationId(),
                    event.papersProcessed());

        } catch (Exception e) {
            log.error(
                    "❌ Failed to process content extraction results for correlation ID: {} - Error: {}",
                    event.correlationId(),
                    e.getMessage(),
                    e);
            throw new RuntimeException("Failed to process content extraction results", e);
        }
    }
} 