package dev.project.scholar_ai.config;

import lombok.Getter;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures RabbitMQ beans for message queuing within the ScholarAI
 * application.
 * This class sets up exchanges, queues, bindings, and message converters
 * to enable asynchronous communication between different services.
 */
@Configuration
@Getter
public class RabbitMQConfig {

    @Value("${scholarai.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${scholarai.rabbitmq.paper-fetch.queue}")
    private String paperFetchQueue;

    @Value("${scholarai.rabbitmq.paper-fetch.routing-key}")
    private String paperFetchRoutingKey;

    @Value("${scholarai.rabbitmq.summarization.queue}")
    private String summarizationQueue;

    @Value("${scholarai.rabbitmq.summarization.routing-key}")
    private String summarizationRoutingKey;

    @Value("${scholarai.rabbitmq.gap-analysis.queue}")
    private String gapAnalysisQueue;

    @Value("${scholarai.rabbitmq.gap-analysis.routing-key}")
    private String gapAnalysisRoutingKey;

    @Value("${scholarai.rabbitmq.paper-fetch.completed-queue}")
    private String paperFetchCompletedQueue;

    @Value("${scholarai.rabbitmq.paper-fetch.completed-routing-key}")
    private String paperFetchCompletedRoutingKey;

    @Value("${scholarai.rabbitmq.summarization.completed-queue}")
    private String summarizationCompletedQueue;

    @Value("${scholarai.rabbitmq.summarization.completed-routing-key}")
    private String summarizationCompletedRoutingKey;

    @Value("${scholarai.rabbitmq.gap-analysis.completed-queue}")
    private String gapAnalysisCompletedQueue;

    @Value("${scholarai.rabbitmq.gap-analysis.completed-routing-key}")
    private String gapAnalysisCompletedRoutingKey;

    @Value("${scholarai.rabbitmq.web-search.queue}")
    private String webSearchQueue;

    @Value("${scholarai.rabbitmq.web-search.routing-key}")
    private String webSearchRoutingKey;

    @Value("${scholarai.rabbitmq.web-search.completed-queue}")
    private String webSearchCompletedQueue;

    @Value("${scholarai.rabbitmq.web-search.completed-routing-key}")
    private String webSearchCompletedRoutingKey;

    /**
     * Creates a durable topic exchange for the application.
     * Topic exchanges route messages based on wildcard matches between the routing
     * key and routing patterns.
     *
     * @return The configured TopicExchange.
     */
    @Bean
    public TopicExchange appExchange() {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    /**
     * Creates a durable queue for paper fetching tasks.
     * Durable queues persist messages even if the RabbitMQ server restarts.
     *
     * @return The configured Queue for paper fetching.
     */
    @Bean
    public Queue paperFetchQueue() {
        return QueueBuilder.durable(paperFetchQueue).build();
    }

    /**
     * Creates a durable queue for summarization tasks.
     *
     * @return The configured Queue for summarization.
     */
    @Bean
    public Queue summarizationQueue() {
        return QueueBuilder.durable(summarizationQueue).build();
    }

    /**
     * Creates a durable queue for gap analysis tasks.
     *
     * @return The configured Queue for gap analysis.
     */
    @Bean
    public Queue gapAnalysisQueue() {
        return QueueBuilder.durable(gapAnalysisQueue).build();
    }

    /**
     * Creates a durable queue for web search tasks.
     *
     * @return The configured Queue for web search.
     */
    @Bean
    public Queue webSearchQueue() {
        return QueueBuilder.durable(webSearchQueue).build();
    }

    /**
     * Creates a durable queue for completed paper fetching tasks.
     *
     * @return The configured Queue for completed paper fetching.
     */
    @Bean
    public Queue paperFetchCompletedQueue() {
        return QueueBuilder.durable(paperFetchCompletedQueue).build();
    }

    /**
     * Creates a durable queue for completed summarization tasks.
     *
     * @return The configured Queue for completed summarization.
     */
    @Bean
    public Queue summarizationCompletedQueue() {
        return QueueBuilder.durable(summarizationCompletedQueue).build();
    }

    /**
     * Creates a durable queue for completed gap analysis tasks.
     *
     * @return The configured Queue for completed gap analysis.
     */
    @Bean
    public Queue gapAnalysisCompletedQueue() {
        return QueueBuilder.durable(gapAnalysisCompletedQueue).build();
    }

    /**
     * Creates a durable queue for completed web search tasks.
     *
     * @return The configured Queue for completed web search.
     */
    @Bean
    public Queue webSearchCompletedQueue() {
        return QueueBuilder.durable(webSearchCompletedQueue).build();
    }

    /**
     * Binds the paper fetch queue to the application exchange using its specific
     * routing key.
     *
     * @param paperFetchQueue The queue for paper fetching tasks.
     * @param appExchange     The main application topic exchange.
     * @return The Binding definition.
     */
    @Bean
    public Binding bindPaperFetch(Queue paperFetchQueue, TopicExchange appExchange) {
        return BindingBuilder.bind(paperFetchQueue).to(appExchange).with(paperFetchRoutingKey);
    }

    /**
     * Binds the summarization queue to the application exchange using its specific
     * routing key.
     *
     * @param summarizationQueue The queue for summarization tasks.
     * @param appExchange        The main application topic exchange.
     * @return The Binding definition.
     */
    @Bean
    public Binding bindSummarization(Queue summarizationQueue, TopicExchange appExchange) {
        return BindingBuilder.bind(summarizationQueue).to(appExchange).with(summarizationRoutingKey);
    }

    /**
     * Binds the gap analysis queue to the application exchange using its specific
     * routing key.
     *
     * @param gapAnalysisQueue The queue for gap analysis tasks.
     * @param appExchange      The main application topic exchange.
     * @return The Binding definition.
     */
    @Bean
    public Binding bindGapAnalysis(Queue gapAnalysisQueue, TopicExchange appExchange) {
        return BindingBuilder.bind(gapAnalysisQueue).to(appExchange).with(gapAnalysisRoutingKey);
    }

    /**
     * Binds the web search queue to the application exchange using its specific
     * routing key.
     *
     * @param webSearchQueue The queue for web search tasks.
     * @param appExchange    The main application topic exchange.
     * @return The Binding definition.
     */
    @Bean
    public Binding bindWebSearch(Queue webSearchQueue, TopicExchange appExchange) {
        return BindingBuilder.bind(webSearchQueue).to(appExchange).with(webSearchRoutingKey);
    }

    /**
     * Binds the paper fetch completed queue to the application exchange using its
     * specific routing key.
     *
     * @param paperFetchCompletedQueue The queue for completed paper fetching tasks.
     * @param appExchange              The main application topic exchange.
     * @return The Binding definition.
     */
    @Bean
    public Binding bindPaperFetchCompleted(Queue paperFetchCompletedQueue, TopicExchange appExchange) {
        return BindingBuilder.bind(paperFetchCompletedQueue).to(appExchange).with(paperFetchCompletedRoutingKey);
    }

    /**
     * Binds the summarization completed queue to the application exchange using its
     * specific routing key.
     *
     * @param summarizationCompletedQueue The queue for completed summarization
     *                                    tasks.
     * @param appExchange                 The main application topic exchange.
     * @return The Binding definition.
     */
    @Bean
    public Binding bindSummarizationCompleted(Queue summarizationCompletedQueue, TopicExchange appExchange) {
        return BindingBuilder.bind(summarizationCompletedQueue).to(appExchange).with(summarizationCompletedRoutingKey);
    }

    /**
     * Binds the gap analysis completed queue to the application exchange using its
     * specific routing key.
     *
     * @param gapAnalysisCompletedQueue The queue for completed gap analysis tasks.
     * @param appExchange               The main application topic exchange.
     * @return The Binding definition.
     */
    @Bean
    public Binding bindGapAnalysisCompleted(Queue gapAnalysisCompletedQueue, TopicExchange appExchange) {
        return BindingBuilder.bind(gapAnalysisCompletedQueue).to(appExchange).with(gapAnalysisCompletedRoutingKey);
    }

    /**
     * Binds the web search completed queue to the application exchange using its
     * specific routing key.
     *
     * @param webSearchCompletedQueue The queue for completed web search tasks.
     * @param appExchange             The main application topic exchange.
     * @return The Binding definition.
     */
    @Bean
    public Binding bindWebSearchCompleted(Queue webSearchCompletedQueue, TopicExchange appExchange) {
        return BindingBuilder.bind(webSearchCompletedQueue).to(appExchange).with(webSearchCompletedRoutingKey);
    }

    /**
     * Creates a message converter to serialize and deserialize messages as JSON.
     * This allows Java objects to be sent and received as JSON payloads.
     *
     * @return The Jackson2JsonMessageConverter instance.
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates a RabbitTemplate configured with a JSON message converter.
     * The RabbitTemplate provides helper methods for sending and receiving
     * messages.
     *
     * @param cf The connection factory for RabbitMQ.
     * @return The configured RabbitTemplate.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        var rt = new RabbitTemplate(cf);
        rt.setMessageConverter(jsonMessageConverter());
        return rt;
    }

    /**
     * Configures the listener container factory for message listeners.
     * This factory sets up concurrency, message conversion, and error handling
     * for RabbitMQ message listeners.
     *
     * @param cf The connection factory for RabbitMQ.
     * @return The configured SimpleRabbitListenerContainerFactory.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory listenerFactory(ConnectionFactory cf) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setDefaultRequeueRejected(false); // send bad messages to DLQ
        return factory;
    }
}