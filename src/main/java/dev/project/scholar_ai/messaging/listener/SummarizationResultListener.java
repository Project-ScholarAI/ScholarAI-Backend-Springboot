package dev.project.scholar_ai.messaging.listener;

import dev.project.scholar_ai.dto.event.SummarizationCompletedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SummarizationResultListener {

    @RabbitListener(
            queues = "${scholarai.rabbitmq.summarization.completed-queue}",
            containerFactory = "listenerFactory")
    @Transactional
    public void onSummarizationCompleted(SummarizationCompletedEvent evt) {
        // persist summaryText into your summaries table:
        // summaryRepo.save(evt.paperId(), evt.summaryText());

        // then kick off GapAnalysisRequestSender.send(...)
    }
}
