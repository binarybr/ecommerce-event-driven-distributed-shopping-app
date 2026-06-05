# ========================================
# Dockerfile for Inventory Service
# ========================================
#
# This Dockerfile containerizes the Inventory Service microservice
#
# SERVICE PURPOSE:
# - Manages product inventory (stock levels)
# - Stock availability checking
# - Inventory reservation when orders placed
# - Event-driven event publishing
# - Consumes order-placed events from Kafka
#
# RUNTIME PORT: 8082
#
# DEPENDENCIES (at runtime):
# - MySQL (inventory_service database)
# - Kafka broker (event messaging)
# - Eureka Discovery Server (service registration)
# - Order Service (calls this via Feign for stock checks)
#
# BUILD COMMAND:
# docker build -t online-shopping/inventory-service:0.0.1-SNAPSHOT .
#
# RUN COMMAND (standalone):
# docker run -e INVENTORY_DATASOURCE_URL=jdbc:mysql://localhost:3306/inventory_service \
#            -e KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
#            -e EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka \
#            -p 8082:8082 \
#            online-shopping/inventory-service:0.0.1-SNAPSHOT
#

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar

# ========================================
# EXPOSE PORT 8082
# ========================================
# Port 8082: Inventory Service REST API
# Accessible at: http://localhost:8082/api/inventory
# Or via API Gateway: http://localhost:8080/api/inventory
#
# Key endpoint: GET /api/inventory?productId=XXX&quantity=YYY
# Called by Order Service to check stock availability
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
