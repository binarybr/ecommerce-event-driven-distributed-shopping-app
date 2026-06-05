# ========================================
# Dockerfile for Discovery Server (Eureka)
# ========================================
#
# This Dockerfile containerizes the Service Discovery Server
#
# SERVICE PURPOSE:
# - Eureka server for service registration and discovery
# - Maintains registry of all microservices
# - Provides service location information to clients
# - Health checking and heartbeat monitoring
# - Dynamic service instance management
#
# RUNTIME PORT: 8761
#
# DEPENDENCIES (at runtime):
# - None (independent component)
#
# BUILD COMMAND:
# docker build -t online-shopping/discovery-server:0.0.1-SNAPSHOT .
#
# RUN COMMAND:
# docker run -p 8761:8761 \
#            online-shopping/discovery-server:0.0.1-SNAPSHOT
#
# EUREKA DASHBOARD:
# Available at: http://localhost:8761
# Shows all registered microservices and their health status
#
# HOW IT WORKS:
# 1. Discovery Server starts and exposes Eureka registry
# 2. Other services register themselves on startup
# 3. API Gateway queries registry to locate backend services
# 4. Services heartbeat every 30 seconds (default)
# 5. If heartbeat missed, service marked as DOWN
#

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar

# ========================================
# EXPOSE PORT 8761
# ========================================
# Port 8761: Eureka Dashboard and Registry
# Accessible at: http://localhost:8761
# Shows all registered services and their instances
EXPOSE 8761

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
