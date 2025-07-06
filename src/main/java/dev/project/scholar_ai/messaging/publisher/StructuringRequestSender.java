package dev.project.scholar_ai.messaging.publisher;

import dev.project.scholar_ai.config.RabbitMQConfig;
import dev.project.scholar_ai.dto.agent.request.StructuringRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StructuringRequestSender {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;

    public void send(StructuringRequest request) {
        try {
            log.info("📤 Sending text structuring request for paper: {} with correlation ID: {}", 
                    request.getPaperId(), request.getCorrelationId());
            
            rabbitTemplate.convertAndSend(
                rabbitMQConfig.getExchangeName(),
                rabbitMQConfig.getStructuringRoutingKey(),
                request
            );
            
            log.info("✅ Text structuring request sent successfully for paper: {}", request.getPaperId());
            
        } catch (Exception e) {
            log.error("❌ Failed to send text structuring request for paper: {} - Error: {}", 
                    request.getPaperId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send text structuring request", e);
        }
    }
}