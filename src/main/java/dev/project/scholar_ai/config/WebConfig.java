package dev.project.scholar_ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for web-related settings.
 * This class implements {@link WebMvcConfigurer} to customize Spring MVC,
 * specifically for CORS configuration.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Array of allowed origins for CORS.
     * The value is injected from the application properties (cors.allowed-origins)
     * or defaults to "http://localhost:3000,
     * https://*.github.dev,https://twiggle.tech,https://*.vercel.app".
     */
    @Value("${cors.allowed-origins:http://localhost:3000, https://*.github.dev,https://twiggle.tech,https://*.vercel.app}")
    private String[] allowedOrigins;

    /**
     * Configures CORS mappings.
     * This method defines the CORS policy for "/api/**" paths, specifying allowed
     * origins, methods, headers, and other CORS-related settings.
     *
     * @param registry The {@link CorsRegistry} to which CORS configurations are
     *                 added.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 1 hour max age
    }
}
