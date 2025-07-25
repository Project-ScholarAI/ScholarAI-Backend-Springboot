package dev.project.scholar_ai.messaging.listener;

import dev.project.scholar_ai.dto.agent.response.StructuringResult;
import dev.project.scholar_ai.service.structuring.StructuringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StructuringResultListener {

    private final StructuringService structuringService;

    @RabbitListener(queues = "${scholarai.rabbitmq.structuring.completed-queue}", containerFactory = "listenerFactory")
    @Transactional(transactionManager = "paperTransactionManager")
    public void onStructuringCompleted(StructuringResult result) {
        log.info(
                "🏗️ Received text structuring result - Paper ID: {}, Correlation ID: {}, Status: {}",
                result.getPaperId(),
                result.getCorrelationId(),
                result.getStatus());

        try {
            // Update the paper with structured content
            structuringService.updatePaperWithStructuredContent(result);

            if ("COMPLETED".equals(result.getStatus())) {
                log.info(
                        "✅ Text structuring completed successfully for paper ID: {} - Sections: {}, Model: {}",
                        result.getPaperId(),
                        result.getSectionsCount(),
                        result.getProcessingModel());
            } else {
                log.warn(
                        "⚠️ Text structuring failed for paper ID: {} - Status: {}, Error: {}",
                        result.getPaperId(),
                        result.getStatus(),
                        result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error(
                    "❌ Failed to process structuring results for paper ID: {} - Error: {}",
                    result.getPaperId(),
                    e.getMessage(),
                    e);
            throw new RuntimeException("Failed to process structuring results", e);
        }
    }
}
