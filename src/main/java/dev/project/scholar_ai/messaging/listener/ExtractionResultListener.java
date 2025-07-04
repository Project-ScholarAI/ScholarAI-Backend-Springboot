package dev.project.scholar_ai.messaging.listener;

import dev.project.scholar_ai.dto.agent.response.ExtractionResult;
import dev.project.scholar_ai.enums.ExtractionStatus;
import dev.project.scholar_ai.service.extraction.ExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractionResultListener {

    private final ExtractionService extractionService;

    @RabbitListener(queues = "${scholarai.rabbitmq.extraction.completed-queue}", containerFactory = "listenerFactory")
    @Transactional(transactionManager = "paperTransactionManager")
    public void onExtractionCompleted(ExtractionResult result) {
        log.info(
                "📄 Received text extraction result - Paper ID: {}, Correlation ID: {}, Status: {}",
                result.getPaperId(),
                result.getCorrelationId(),
                result.getStatus());

        try {
            // Update the paper with extracted text
            extractionService.updatePaperWithExtractedText(result);

            if ("COMPLETED".equals(result.getStatus()) && result.getExtractedText() != null) {
                log.info(
                        "✅ Text extraction completed successfully for paper ID: {} - Text length: {} characters, Method: {}",
                        result.getPaperId(),
                        result.getTextLength(),
                        result.getExtractionMethod());

                // Trigger summarization if text extraction was successful
                extractionService.triggerSummarization(result.getPaperId(), result.getExtractedText());
                
            } else {
                log.warn(
                        "⚠️ Text extraction failed or incomplete for paper ID: {} - Status: {}, Error: {}",
                        result.getPaperId(),
                        result.getStatus(),
                        result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error(
                    "❌ Failed to process extraction results for paper ID: {} - Error: {}",
                    result.getPaperId(),
                    e.getMessage(),
                    e);
            throw new RuntimeException("Failed to process extraction results", e);
        }
    }
}