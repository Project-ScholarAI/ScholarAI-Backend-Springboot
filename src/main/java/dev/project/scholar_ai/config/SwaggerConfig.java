package dev.project.scholar_ai.config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger API documentation.
 * This class sets up grouped API documentation for Actuator and Application
 * endpoints.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Group name for Actuator API documentation.
     */
    private static final String ACTUATOR_GROUP = "Actuator API";
    /**
     * Group name for Application API documentation.
     */
    private static final String APPLICATION_GROUP = "Application API";
    /**
     * Path pattern for Actuator endpoints.
     */
    private static final String ACTUATOR_PATH_PATTERN = "/actuator/**";
    /**
     * Path pattern for Application endpoints.
     */
    private static final String APPLICATION_PATH_PATTERN = "/api/**";
    /**
     * Version of the API.
     */
    private static final String API_VERSION = "1.0";

    /**
     * Creates a {@link GroupedOpenApi} bean for the Actuator endpoints.
     * This group includes all paths under "/actuator/**" and provides
     * specific documentation details for these management endpoints.
     *
     * @return A {@link GroupedOpenApi} instance for actuator API documentation.
     */
    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder()
                .group(ACTUATOR_GROUP)
                .pathsToMatch(ACTUATOR_PATH_PATTERN)
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("Actuator API Documentation")
                        .description("API endpoints for application monitoring and management")
                        .version(API_VERSION)))
                .build();
    }

    /**
     * Creates a {@link GroupedOpenApi} bean for the main application API endpoints.
     * This group includes all paths under "/api/**" and provides
     * documentation for the core ScholarAI application functionalities.
     *
     * @return A {@link GroupedOpenApi} instance for application API documentation.
     */
    @Bean
    public GroupedOpenApi applicationApi() {
        return GroupedOpenApi.builder()
                .group(APPLICATION_GROUP)
                .pathsToMatch(APPLICATION_PATH_PATTERN)
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("ScholarAI API Documentation")
                        .description("API endpoints for ScholarAI application")
                        .version(API_VERSION)))
                .build();
    }
}
