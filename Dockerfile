# Stage 1: Build the application
FROM gradle:8-jdk17 AS builder
WORKDIR /app

# Copy the Gradle configuration to cache dependencies
COPY build.gradle settings.gradle ./
COPY gradle gradle
# Run a dry build to download dependencies (optional but recommended for caching)
RUN gradle build --no-daemon -x test --continue || true

# Copy the source code
COPY src src

# Build the application
# Skipping tests to speed up the build in Docker, assuming tests are run in CI/CD
RUN gradle build --no-daemon -x test

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from the builder stage
# The jar path might vary depending on the version, using a wildcard to be safe or assuming the standard build output
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port (default for Spring Boot is 8080)
EXPOSE 8080

# Environment variables for configuration
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
