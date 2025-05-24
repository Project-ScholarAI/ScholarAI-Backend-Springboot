package dev.project.scholar_ai.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for RateLimiter beans.
 */
@Configuration
public class RateLimiterConfiguration {

    /**
     * Creates a RateLimiterRegistry bean with the default rate limiter
     * configuration.
     *
     * @return The configured RateLimiterRegistry.
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        return RateLimiterRegistry.of(defaultRateLimiterConfig());
    }

    /**
     * Defines the default rate limiter configuration.
     * This configuration allows 300 requests per minute.
     *
     * @return The default RateLimiterConfig.
     */
    @Bean
    public RateLimiterConfig defaultRateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(300)
                .timeoutDuration(Duration.ZERO)
                .build();
    }

    /**
     * Creates a RateLimiter bean for standard API endpoints.
     * This limiter uses the default rate limiter configuration.
     *
     * @param rateLimiterRegistry The RateLimiterRegistry to use.
     * @return The configured RateLimiter for standard APIs.
     */
    @Bean
    public RateLimiter standardApiLimiter(RateLimiterRegistry rateLimiterRegistry) {
        return rateLimiterRegistry.rateLimiter("standard-api");
    }

    /**
     * Creates a RateLimiter bean for testing error handling.
     * This limiter allows 30 requests per 10 seconds.
     *
     * @param rateLimiterRegistry The RateLimiterRegistry to use.
     * @return The configured RateLimiter for error testing.
     */
    @Bean
    public RateLimiter testErrorLimiter(RateLimiterRegistry rateLimiterRegistry) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(30)
                .limitRefreshPeriod(Duration.ofSeconds(10))
                .timeoutDuration(Duration.ZERO)
                .build();

        return rateLimiterRegistry.rateLimiter("test-error", config);
    }

    /**
     * Creates a RateLimiter bean for actuator endpoints.
     * This limiter allows 60 requests per minute.
     *
     * @param rateLimiterRegistry The RateLimiterRegistry to use.
     * @return The configured RateLimiter for actuator endpoints.
     */
    @Bean
    public RateLimiter actuatorLimiter(RateLimiterRegistry rateLimiterRegistry) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(60)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build();

        return rateLimiterRegistry.rateLimiter("actuator", config);
    }
}
