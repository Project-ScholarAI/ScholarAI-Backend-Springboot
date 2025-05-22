package dev.project.scholar_ai.messaging.listener;


import dev.project.scholar_ai.dto.event.GapAnalysisCompletedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GapAnalysisResultListener {

    @RabbitListener(
            queues = "${scholarai.rabbitmq.gap-analysis.completed-queue}",
            containerFactory = "listenerFactory"
    )
    @Transactional
    public void onGapAnalysisCompleted(GapAnalysisCompletedEvent evt) {
        // write identifiedGaps into your gap_analysis table:
        // gapRepo.save(evt.paperId(), evt.identifiedGaps());

        // optionally notify frontend or mark pipeline “done”
    }
}
