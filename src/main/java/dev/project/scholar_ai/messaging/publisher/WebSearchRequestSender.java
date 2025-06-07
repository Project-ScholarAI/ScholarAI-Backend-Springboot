package dev.project.scholar_ai.messaging.publisher;

import dev.project.scholar_ai.config.RabbitMQConfig;
import dev.project.scholar_ai.dto.agent.request.WebSearchRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSearchRequestSender {
    private final RabbitTemplate rt;
    private final RabbitMQConfig rabbitMQConfig;

    public WebSearchRequestSender(RabbitTemplate rabbitTemplate, RabbitMQConfig rabbitMQConfig) {
        this.rt = rabbitTemplate;
        this.rabbitMQConfig = rabbitMQConfig;
    }

    public void send(WebSearchRequest req) {
        rt.convertAndSend(rabbitMQConfig.getExchangeName(), rabbitMQConfig.getWebSearchRoutingKey(), req);
    }
}
