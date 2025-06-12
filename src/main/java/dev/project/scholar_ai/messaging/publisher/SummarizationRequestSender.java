package dev.project.scholar_ai.messaging.publisher;

import dev.project.scholar_ai.config.RabbitMQConfig;
import dev.project.scholar_ai.dto.agent.request.SummarizationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummarizationRequestSender {
    private final RabbitTemplate rt;
    private final RabbitMQConfig rabbitMQConfig;

    public void send(SummarizationRequest req) {
        rt.convertAndSend(rabbitMQConfig.getExchangeName(), rabbitMQConfig.getSummarizationRoutingKey(), req);
    }
}
