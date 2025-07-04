package dev.project.scholar_ai.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration with mandatory password authentication.
 */
@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host}")
    private String redisHost;         // no default → will fail if missing

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;     // no default → will fail if missing

    // -------------------------------------------------------------------------
    // Connection factory
    // -------------------------------------------------------------------------
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        validateProps();              // abort if a required prop is empty

        RedisStandaloneConfiguration cfg =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        cfg.setUsername("default");                       // ACL user (Redis ≥ 6)
        cfg.setPassword(RedisPassword.of(redisPassword)); // always send password

        LettuceClientConfiguration clientCfg = LettuceClientConfiguration.builder()
                // Force RESP2 so that CLIENT SETINFO isn’t used (for Redis < 7)
                .clientOptions(ClientOptions.builder()
                        .protocolVersion(ProtocolVersion.RESP2)
                        .build())
                .build();

        return new LettuceConnectionFactory(cfg, clientCfg);
    }

    // -------------------------------------------------------------------------
    // RedisTemplate
    // -------------------------------------------------------------------------
    @Bean
    public RedisTemplate<String, String> redisTemplate(
            LettuceConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private void validateProps() {
        if (redisHost == null || redisHost.isBlank()) {
            throw new IllegalStateException(
                    "`spring.data.redis.host` must be configured");
        }
        if (redisPassword == null || redisPassword.isBlank()) {
            throw new IllegalStateException(
                    "`spring.data.redis.password` must be configured — Redis must be protected by a password");
        }

        log.info("✅ Redis Host: {}", redisHost);
        log.info("✅ Redis Port: {}", redisPort);
        log.info("🔒 Redis password is configured (not logged for security).");
    }
}
