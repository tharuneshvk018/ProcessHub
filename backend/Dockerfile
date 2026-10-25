# ==============================================================================
# ProcessHub Backend - Multi-stage Production Dockerfile
# ==============================================================================

# Stage 1: Build & Package
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy Maven POM and dependency offline resolution
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code
COPY src ./src

# Build production JAR (skipping unit tests during image build; verified via CI)
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Distroless / Temurin JRE Runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create dedicated non-root application user and group
RUN addgroup -S processhub && adduser -S processhub -G processhub

# Set working directory
WORKDIR /app

# Copy executable jar from builder stage
COPY --from=builder /build/target/processhub-backend-*.jar /app/app.jar

# Setup upload attachments storage directory with ownership
RUN mkdir -p /app/uploads && chown -R processhub:processhub /app

# Switch to non-root user
USER processhub

# Expose HTTP port
EXPOSE 8080

# Configure JVM memory limits and production profile
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="prod"

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
