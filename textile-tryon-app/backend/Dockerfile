# RAMRAJ Textile Try-On Backend Dockerfile
# Multi-stage build for optimized production image

# Build stage
FROM maven:3.9-openjdk-17-slim AS build

WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies (cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Production stage
FROM openjdk:17-jdk-slim AS production

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user for security
RUN addgroup --system ramraj && adduser --system --group ramraj

WORKDIR /app

# Create uploads directory
RUN mkdir -p uploads && chown -R ramraj:ramraj uploads

# Copy JAR file from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to app user
RUN chown -R ramraj:ramraj /app

# Switch to app user
USER ramraj

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# Environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
