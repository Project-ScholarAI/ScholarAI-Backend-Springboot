package dev.project.scholar_ai.messaging.listener;

import dev.project.scholar_ai.dto.event.SummarizationCompletedEvent;
import dev.project.scholar_ai.service.summarizer.SummarizerResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummarizationResultListener {

    private final SummarizerResultService summarizerResultService;

    @RabbitListener(
            queues = "${scholarai.rabbitmq.summarization.completed-queue}",
            containerFactory = "listenerFactory")
    @Transactional(transactionManager = "paperTransactionManager")
    public void onSummarizationCompleted(SummarizationCompletedEvent evt) {
        log.info("📄 Summarization completion received: correlationId={}, processed={}, failed={}",
                evt.correlationId(), evt.processed(), evt.failed());
        summarizerResultService.handleCompletion(evt);
    }
}
