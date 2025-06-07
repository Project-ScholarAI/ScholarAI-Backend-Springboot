###############################################################################
# 1️⃣  Dependency-cache stage (unchanged)
###############################################################################
FROM maven:3.9-amazoncorretto-21-alpine AS deps
WORKDIR /app
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp dependency:go-offline

###############################################################################
# 2️⃣  Build & test stage (unchanged)
###############################################################################
FROM deps AS build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
  mvn -B -ntp spotless:apply verify -DskipITs=false

###############################################################################
# 3️⃣  Runtime stage (Sonar-clean)
###############################################################################
FROM amazoncorretto:21-alpine AS runtime

# Non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Build-time ARG for profile (used in COPY)
ARG ENV
ENV SPRING_PROFILES_ACTIVE=$ENV

# ---- Sonar fix #1: install packages with --no-cache -------------------------
RUN apk add --no-cache curl
# ----------------------------------------------------------------------------

# App + configs
COPY --from=build /app/target/scholar-ai.jar  app.jar
COPY --from=build /app/src/main/resources/application.yml              ./
COPY --from=build /app/src/main/resources/application-${ENV}.yml       ./application-${ENV}.yml

EXPOSE 8080
USER spring

# ---- Sonar fix #2: exec-form HEALTHCHECK CMD -------------------------------
HEALTHCHECK --start-period=30s --interval=30s --timeout=3s --retries=3 \
  CMD ["curl", "--fail", "http://localhost:8080/actuator/health"]
# ----------------------------------------------------------------------------

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:+AlwaysPreTouch", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
