package dev.project.scholar_ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for Redis.
 *
 * <p>
 * This class sets up the connection to a Redis instance and provides a
 * RedisTemplate
 * for interacting with Redis. It reads Redis connection details (host, port,
 * password)
 * from application properties.
 */
@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host:}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    /**
     * Creates a LettuceConnectionFactory for connecting to Redis.
     *
     * <p>
     * This method configures the connection using the host, port, and password
     * specified in the application properties. It logs the Redis environment
     * variables
     * being used.
     *
     * @return A configured LettuceConnectionFactory instance.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        logRedisEnv();
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        if (!redisPassword.isBlank()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }

    /**
     * Creates a RedisTemplate for interacting with Redis.
     *
     * <p>
     * This method configures the RedisTemplate with the provided
     * LettuceConnectionFactory
     * and sets StringRedisSerializer for both keys and values.
     *
     * @param connectionFactory The LettuceConnectionFactory to use for the
     *                          RedisTemplate.
     * @return A configured RedisTemplate instance.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * Logs the Redis environment variables.
     *
     * <p>
     * This method logs the Redis host, port, and whether a password is set.
     * It issues warnings if the host or password is not set.
     */
    private void logRedisEnv() {
        if (redisHost.isBlank()) {
            logger.warn("⚠️  Redis host is not set (spring.data.redis.host).");
        } else {
            logger.info("✅ Redis Host: {}", redisHost);
        }

        logger.info("✅ Redis Port: {}", redisPort);

        if (redisPassword.isBlank()) {
            logger.warn("⚠️  Redis password is not set (spring.data.redis.password).");
        } else {
            logger.info("🔒 Redis password is set.");
        }
    }
}
