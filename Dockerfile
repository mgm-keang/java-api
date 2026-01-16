# --- Stage 1: Build the Application ---
FROM gradle:8.10-jdk21 AS builder
WORKDIR /app

# Copy gradle settings first to leverage caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (this step is cached if dependencies don't change)
RUN gradle clean build --no-daemon -x test || return 0

# Copy the actual source code (including your application.properties)
COPY src ./src

# Build the JAR file
RUN gradle bootJar --no-daemon -x test

# --- Stage 2: Run the Application ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Command to start the application
ENTRYPOINT ["java", "-jar", "app.jar"]