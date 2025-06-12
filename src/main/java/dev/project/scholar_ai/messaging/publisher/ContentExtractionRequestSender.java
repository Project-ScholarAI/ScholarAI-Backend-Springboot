package dev.project.scholar_ai.messaging.publisher;

import dev.project.scholar_ai.config.RabbitMQConfig;
import dev.project.scholar_ai.dto.agent.request.ContentExtractionRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ContentExtractionRequestSender {
    private final RabbitTemplate rt;
    private final RabbitMQConfig rabbitMQConfig;

    public ContentExtractionRequestSender(RabbitTemplate rabbitTemplate, RabbitMQConfig rabbitMQConfig) {
        this.rt = rabbitTemplate;
        this.rabbitMQConfig = rabbitMQConfig;
    }

    public void send(ContentExtractionRequest req) {
        // TODO: Add proper configuration for content extraction queues
        rt.convertAndSend(rabbitMQConfig.getExchangeName(), "scholarai.content-extraction", req);
    }
} 