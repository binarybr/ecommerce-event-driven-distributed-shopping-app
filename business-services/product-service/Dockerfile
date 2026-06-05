    # ========================================
# Dockerfile for Product Service
# ========================================
#
# Multi-stage Docker build for Product Service microservice
# This Dockerfile creates a containerized version of the Product Service
#
# BUILD PROCESS:
# 1. Base image: Eclipse Temurin JRE 25 on Alpine Linux (minimal, ~150MB)
# 2. Copy pre-built JAR from target/ directory
# 3. Expose port 8081
# 4. Set entry point to launch with java -jar command
#
# PREREQUISITES:
# - JAR must be built first: mvn clean package
# - JAR location: target/product-service-0.0.1-SNAPSHOT.jar
#
# BUILD COMMAND:
# docker build -t online-shopping/product-service:0.0.1-SNAPSHOT .
#
# RUN COMMAND:
# docker run -e MONGODB_URI=mongodb://mongo:27017/product_service \
#            -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
#            -e EUREKA_DEFAULT_ZONE=http://discovery-server:8761/eureka \
#            -p 8081:8081 \
#            online-shopping/product-service:0.0.1-SNAPSHOT
#
# IN DOCKER COMPOSE:
# - docker-compose automatically builds and runs this service
# - Environment variables are set in docker-compose.yml
# - Service name resolves to hostname: product-service
#

# ========================================
# BASE IMAGE: Eclipse Temurin JRE 25 Alpine
# ========================================
# Why Alpine?: Minimal OS (~5MB base)
# Why JRE only?: No compilation needed at runtime
# Size: ~150-180MB total (small enough for container registries)
# Java Version: 25 (latest LTS update)
FROM eclipse-temurin:25-jre-alpine

# ========================================
# SET WORKING DIRECTORY
# ========================================
# All subsequent commands run from /app directory
WORKDIR /app

# ========================================
# COPY APPLICATION JAR
# ========================================
# Copy the built Spring Boot JAR from target/ directory
# Glob pattern: *.jar (matches any .jar file)
# Rename to: app.jar (simpler name for startup)
# Source: Built by Maven in: target/product-service-0.0.1-SNAPSHOT.jar
COPY target/*.jar app.jar

# ========================================
# EXPOSE PORT
# ========================================
# Port 8081: Product Service HTTP port
# This documents the port (doesn't actually listen until runtime)
# Docker Compose will map this to host if needed
# API endpoints accessible at: http://localhost:8081/api/products
EXPOSE 8081

# ========================================
# ENTRYPOINT: STARTUP COMMAND
# ========================================
# Entry point: java -jar /app/app.jar
# This starts the Spring Boot application when container starts
#
# JAVA OPTIONS (can be passed at runtime):
# - -Xmx512m: Maximum heap size
# - -Xms256m: Initial heap size
# - -XX:+UseG1GC: G1 garbage collector (efficient for microservices)
#
# To pass options: docker run -e JAVA_OPTS="-Xmx1g" ...
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# ========================================
# HEALTH CHECK (optional, can be added)
# ========================================
# For production, consider adding health check:
# HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
#   CMD curl -f http://localhost:8081/actuator/health || exit 1
#
# This would allow Docker to track container health status

