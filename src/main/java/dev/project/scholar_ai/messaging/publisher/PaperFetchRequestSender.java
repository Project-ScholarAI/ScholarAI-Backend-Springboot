package dev.project.scholar_ai.messaging.publisher;

import dev.project.scholar_ai.config.RabbitMQConfig;
import dev.project.scholar_ai.dto.agent.request.PaperFetchRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaperFetchRequestSender {
    private final RabbitTemplate rt;
    private final RabbitMQConfig rabbitMQConfig;

    public PaperFetchRequestSender(RabbitTemplate rabbitTemplate, RabbitMQConfig rabbitMQConfig) {
        this.rt = rabbitTemplate;
        this.rabbitMQConfig = rabbitMQConfig;
    }

    public void send(PaperFetchRequest req) {
        rt.convertAndSend(rabbitMQConfig.getExchangeName(), rabbitMQConfig.getPaperFetchRoutingKey(), req);
    }
}
