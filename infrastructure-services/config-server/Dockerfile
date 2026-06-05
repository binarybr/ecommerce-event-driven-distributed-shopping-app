# ========================================
# Dockerfile for Config Server
# ========================================
#
# This Dockerfile containerizes the Centralized Configuration Server
#
# SERVICE PURPOSE:
# - Centralized configuration management for all microservices
# - Configuration version control (stored in Git)
# - Dynamic configuration updates without redeployment
# - Environment-specific configurations (dev, staging, prod)
# - Profiles: development, docker, kubernetes
#
# RUNTIME PORT: 8888
#
# DEPENDENCIES (at runtime):
# - Git repository (for configuration source)
# - Discovery Server (for service registration)
#
# BUILD COMMAND:
# docker build -t online-shopping/config-server:0.0.1-SNAPSHOT .
#
# RUN COMMAND:
# docker run -p 8888:8888 \
#            -e SPRING_PROFILES_ACTIVE=docker \
#            online-shopping/config-server:0.0.1-SNAPSHOT
#
# CONFIGURATION RETRIEVAL:
# Other services fetch config via HTTP GET requests:
# GET http://config-server:8888/{service-name}/{profile}
#
# Example:
# GET http://config-server:8888/product-service/docker
# GET http://config-server:8888/order-service/kubernetes
#
# CONFIG FILES LOCATION:
# Stored in Git repository (typically in config repo)
# Or can use local file system for docker-compose
# Profile-specific files:
# - application.yaml (default)
# - application-docker.yaml (docker environment)
# - application-kubernetes.yaml (kubernetes environment)
#

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar

# ========================================
# EXPOSE PORT 8888
# ========================================
# Port 8888: Config Server API
# Accessible at: http://localhost:8888
#
# Config endpoints:
# GET /service-name/profile - Fetch configuration
# GET /service-name/profile/label - Fetch for specific Git label
# GET /encrypt - Encrypt configuration values
# GET /decrypt - Decrypt configuration values
EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
