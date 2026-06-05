# ========================================
# Dockerfile for Order Service
# ========================================
#
# This Dockerfile containerizes the Order Service microservice
#
# SERVICE PURPOSE:
# - Manages customer order placement
# - Order tracking and status updates
# - Integrates with Inventory Service for stock checking
# - Publishes order events via Kafka
# - Listens for inventory availability changes
#
# RUNTIME PORT: 8083
#
# DEPENDENCIES (at runtime):
# - MySQL (order_service database)
# - Kafka broker (event messaging)
# - Eureka Discovery Server (service registration)
# - Inventory Service (Feign client for stock validation)
#
# BUILD COMMAND:
# docker build -t online-shopping/order-service:0.0.1-SNAPSHOT .
#
# RUN COMMAND (standalone):
# docker run -e ORDER_DATASOURCE_URL=jdbc:mysql://localhost:3306/order_service \
#            -e KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
#            -e EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka \
#            -p 8083:8083 \
#            online-shopping/order-service:0.0.1-SNAPSHOT
#

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar

# ========================================
# EXPOSE PORT 8083
# ========================================
# Port 8083: Order Service REST API
# Accessible at: http://localhost:8083/api/orders
# Or via API Gateway: http://localhost:8080/api/orders
EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
