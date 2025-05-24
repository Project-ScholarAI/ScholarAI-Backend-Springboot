package dev.project.scholar_ai.messaging.listener;

import dev.project.scholar_ai.dto.event.SummarizationCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SummarizationResultListener {

    private static final Logger logger = LoggerFactory.getLogger(SummarizationResultListener.class);

    @RabbitListener(
            queues = "${scholarai.rabbitmq.summarization.completed-queue}",
            containerFactory = "listenerFactory")
    @Transactional
    public void onSummarizationCompleted(SummarizationCompletedEvent evt) {
        logger.info("📄 Received summarization result for paper ID: {}", evt.paperId());
        logger.info("🔗 Correlation ID: {}", evt.correlationId());
        logger.info("📝 Summary length: {} characters", evt.summaryText().length());
        logger.info(
                "✅ Summary preview: {}",
                evt.summaryText().length() > 100 ? evt.summaryText().substring(0, 100) + "..." : evt.summaryText());

        // In a real implementation, you would:
        // 1. Save the summary to database
        // 2. Update the paper status
        // 3. Trigger next step in the pipeline (e.g., gap analysis)
        // 4. Send notifications to users

        logger.info("🎯 Summarization processing completed successfully!");
    }
}
