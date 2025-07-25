package dev.project.scholar_ai.messaging.publisher;

import dev.project.scholar_ai.config.RabbitMQConfig;
import dev.project.scholar_ai.dto.agent.request.ExtractionRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ExtractionRequestSender {
    private final RabbitTemplate rt;
    private final RabbitMQConfig rabbitMQConfig;

    public ExtractionRequestSender(RabbitTemplate rabbitTemplate, RabbitMQConfig rabbitMQConfig) {
        this.rt = rabbitTemplate;
        this.rabbitMQConfig = rabbitMQConfig;
    }

    public void send(ExtractionRequest req) {
        rt.convertAndSend(rabbitMQConfig.getExchangeName(), rabbitMQConfig.getExtractionRoutingKey(), req);
    }
}
