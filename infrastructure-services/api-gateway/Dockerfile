# ========================================
# Dockerfile for API Gateway
# ========================================
#
# This Dockerfile containerizes the API Gateway (routing service)
#
# SERVICE PURPOSE:
# - Single entry point for all external client requests
# - Routes requests to appropriate backend microservices
# - Provides centralized authentication and security
# - Request logging and monitoring
# - Rate limiting and throttling
#
# RUNTIME PORT: 8080 (MAIN ENTRY POINT)
#
# DEPENDENCIES (at runtime):
# - Eureka Discovery Server (service discovery)
# - Backend microservices (Product, Order, Inventory, Notification)
#
# BUILD COMMAND:
# docker build -t online-shopping/api-gateway:0.0.1-SNAPSHOT .
#
# RUN COMMAND:
# docker run -e EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka \
#            -e GATEWAY_USERNAME=admin \
#            -e GATEWAY_PASSWORD=change-me \
#            -p 8080:8080 \
#            online-shopping/api-gateway:0.0.1-SNAPSHOT
#
# AUTHENTICATION:
# Default credentials: admin / change-me
# HTTP Basic Auth: All requests require authentication
# Exceptions: /actuator/health, /actuator/info (public endpoints)
#
# API ROUTES:
# /api/products/** -> Product Service (8081)
# /api/orders/** -> Order Service (8083)
# /api/inventory/** -> Inventory Service (8082)
# /api/notifications/** -> Notification Service (8084)
#

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar

# ========================================
# EXPOSE PORT 8080
# ========================================
# Port 8080: API Gateway REST API
# THIS IS THE MAIN ENTRY POINT FOR ALL CLIENT REQUESTS
# All other microservices are accessed through here
#
# Example requests:
# curl -u admin:change-me http://localhost:8080/api/products
# curl -u admin:change-me http://localhost:8080/api/orders
# curl -u admin:change-me http://localhost:8080/api/inventory
# curl -u admin:change-me http://localhost:8080/api/notifications
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
