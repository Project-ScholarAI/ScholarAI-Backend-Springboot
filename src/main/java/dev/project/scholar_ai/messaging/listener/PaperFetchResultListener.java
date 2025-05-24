package dev.project.scholar_ai.messaging.listener;

import dev.project.scholar_ai.dto.event.PaperFetchCompletedEvent;
import dev.project.scholar_ai.dto.event.PaperMetadata;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaperFetchResultListener {

    @RabbitListener(queues = "${scholarai.rabbitmq.paper-fetch.completed-queue}", containerFactory = "listenerFactory")
    @Transactional
    public void onPaperFetchCompleted(PaperFetchCompletedEvent evt) {
        // 1) Upsert metadata + pdfUrl into your Postgres via JPA or JdbcTemplate
        for (PaperMetadata p : evt.papers()) {
            // example pseudo-code:
            // paperRepository.upsert(p.doi(), p.title(), p.authors(), p.pdfUrl());
        }

        // 2) Publish next command, e.g. ScrapeRequestSender.send(...)
    }
}
