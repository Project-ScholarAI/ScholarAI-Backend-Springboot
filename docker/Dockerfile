### ✅ Modified `Dockerfile` (with improved JVM memory handling)
FROM amazoncorretto:21-alpine AS runtime

# Non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Build-time ARG for profile (used in COPY)
ARG ENV
ENV SPRING_PROFILES_ACTIVE=$ENV

# ---- Required tools ----
RUN apk add --no-cache curl

# App + config
COPY --from=build /app/target/scholar-ai.jar  app.jar
COPY --from=build /app/src/main/resources/application.yml              ./
COPY --from=build /app/src/main/resources/application-${ENV}.yml       ./application-${ENV}.yml

EXPOSE 8080
USER spring

# Healthcheck
HEALTHCHECK --start-period=30s --interval=30s --timeout=3s --retries=3 \
  CMD ["curl", "--fail", "http://localhost:8080/actuator/health"]

# JVM memory tuning + Container Awareness
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:+AlwaysPreTouch", \
  "-XX:InitialRAMPercentage=25.0", \
  "-XX:MaxRAMPercentage=50.0", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-jar", "app.jar"]