# Testing & Deployment Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Build & Compile](#build--compile)
3. [Local Development Setup](#local-development-setup)
4. [Service Startup](#service-startup)
5. [Integration Testing](#integration-testing)
6. [Docker Deployment](#docker-deployment)

---

## Prerequisites

### Required Software
- Java 25+
- Maven 3.8+
- MySQL 8.0+
- MongoDB 5.0+
- Kafka 3.0+
- Git

### System Requirements
- Minimum 8GB RAM
- 20GB disk space
- Ports available: 8080, 8081, 8082, 8083, 8084, 8761, 8888, 9092, 27017, 3306

---

## Build & Compile

### Step 1: Build the entire project
```bash
cd C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app
mvn clean install -DskipTests
```

### Step 2: Build with tests (after setting up databases)
```bash
mvn clean install
```

### Step 3: Build individual services
```bash
# Product Service
mvn clean install -pl :product-service

# Order Service
mvn clean install -pl :order-service

# Inventory Service
mvn clean install -pl :inventory-service

# Notification Service
mvn clean install -pl :notification-service

# API Gateway
mvn clean install -pl :api-gateway

# Discovery Server
mvn clean install -pl :discovery-server

# Config Server
mvn clean install -pl :config-server
```

---

## Local Development Setup

### 1. Database Setup

#### MySQL Databases
```bash
# Connect to MySQL
mysql -u root -p

# Create databases
CREATE DATABASE order_service;
CREATE DATABASE inventory_service;
CREATE DATABASE notification_service;

# Grant permissions
GRANT ALL PRIVILEGES ON order_service.* TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON inventory_service.* TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON notification_service.* TO 'localhost' IDENTIFIED BY 'binary777Code';
FLUSH PRIVILEGES;
```

#### MongoDB Setup
```bash
# Start MongoDB
mongod

# Create database and collection (automatic)
# Database: product_service
# Collection: products (created on first insert)
```

### 2. Kafka Setup

#### Start Zookeeper
```bash
# Windows
bin\windows\zookeeper-server-start.bat config\zookeeper.properties

# Linux/Mac
bin/zookeeper-server-start.sh config/zookeeper.properties
```

#### Start Kafka
```bash
# Windows
bin\windows\kafka-server-start.bat config\server.properties

# Linux/Mac
bin/kafka-server-start.sh config/server.properties
```

#### Create Kafka Topics
```bash
# Windows
bin\windows\kafka-topics.bat --create --topic product-created --bootstrap-server localhost:9092
bin\windows\kafka-topics.bat --create --topic order-placed --bootstrap-server localhost:9092
bin\windows\kafka-topics.bat --create --topic inventory-reserved --bootstrap-server localhost:9092
bin\windows\kafka-topics.bat --create --topic inventory-failed --bootstrap-server localhost:9092

# Linux/Mac
bin/kafka-topics.sh --create --topic product-created --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic order-placed --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic inventory-reserved --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic inventory-failed --bootstrap-server localhost:9092
```

### 3. Email Service Setup (Optional)

#### Option A: Using MailHog
```bash
# Download MailHog: https://github.com/mailhog/MailHog/releases
# Run MailHog (listens on port 1025 for SMTP)
mailhog.exe

# Access UI at http://localhost:8025
```

#### Option B: Using Real SMTP
Update `notification-service/src/main/resources/application.yaml`:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

---

## Service Startup

### Terminal 1: Discovery Server
```bash
cd C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\infrastructure-services\discovery-server
mvn spring-boot:run
# Starts on port 8761
# Access: http://localhost:8761
```

### Terminal 2: Config Server
```bash
cd C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\infrastructure-services\config-server
mvn spring-boot:run
# Starts on port 8888
# Access: http://localhost:8888
```

### Terminal 3: Product Service
```bash
cd C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\business-services\product-service
mvn spring-boot:run
# Starts on port 8081
# Access: http://localhost:8081
```

### Terminal 4: Inventory Service
```bash
cd C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\business-services\inventory-service
mvn spring-boot:run
# Starts on port 8082
# Access: http://localhost:8082
```

### Terminal 5: Order Service
```bash
cd C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\business-services\order-service
mvn spring-boot:run
# Starts on port 8083
# Access: http://localhost:8083
```

### Terminal 6: Notification Service
```bash
cd C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\business-services\notification-service
mvn spring-boot:run
# Starts on port 8084
# Access: http://localhost:8084
```

### Terminal 7: API Gateway
```bash
cd C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\infrastructure-services\api-gateway
mvn spring-boot:run
# Starts on port 8080
# Main entry point: http://localhost:8080
```

---

## Integration Testing

### 1. Verify Services Are Running

```bash
# Through API Gateway
curl http://localhost:8080/actuator/health

# Direct service checks
curl http://localhost:8081/actuator/health  # Product Service
curl http://localhost:8082/actuator/health  # Inventory Service
curl http://localhost:8083/actuator/health  # Order Service
curl http://localhost:8084/actuator/health  # Notification Service
```

### 2. Create a Product

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99
  }'
```

**Expected Response:**
```json
{
  "id": "product-id",
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99
}
```

### 3. Add Inventory

```bash
curl -X POST http://localhost:8080/api/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "product-id",
    "quantity": 100
  }'
```

**Expected Response:** 201 Created

### 4. Check Inventory

```bash
curl http://localhost:8080/api/inventory?productId=product-id&quantity=5
```

**Expected Response:**
```json
{
  "inStock": true
}
```

### 5. Place an Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "product-id",
    "quantity": 5,
    "price": 999.99
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "orderNumber": "uuid-string",
  "productId": "product-id",
  "quantity": 5,
  "price": 999.99,
  "createdAt": "2026-05-18T12:00:00"
}
```

### 6. Verify Event Publishing

#### Monitor Kafka Topics
```bash
# Product Created Events
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic product-created

# Order Placed Events
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-placed

# Inventory Reserved Events
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic inventory-reserved

# Inventory Failed Events
bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic inventory-failed
```

### 7. Verify Notifications Were Stored

```bash
# Check database
mysql -u root -p
USE notification_service;
SELECT * FROM notifications;
```

### 8. Service Discovery Verification

```bash
# Check Eureka dashboard
# Visit: http://localhost:8761

# Should show:
# - product-service (UP)
# - order-service (UP)
# - inventory-service (UP)
# - notification-service (UP)
# - api-gateway (UP)
```

---

## Docker Deployment

### 1. Create Dockerfile for Each Service

#### Product Service Dockerfile
```dockerfile
FROM openjdk:25-jdk-slim
WORKDIR /app
COPY business-services/product-service/target/product-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Similar for other services (8082, 8083, 8084, 8080, 8761, 8888)

### 2. Docker Compose Setup

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  mongodb:
    image: mongo:5.0
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db

  kafka:
    image: confluentinc/cp-kafka:latest
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  discovery-server:
    build: infrastructure-services/discovery-server
    ports:
      - "8761:8761"

  config-server:
    build: infrastructure-services/config-server
    ports:
      - "8888:8888"

  product-service:
    build: business-services/product-service
    ports:
      - "8081:8081"
    environment:
      EUREKA_DEFAULT_ZONE: http://discovery-server:8761/eureka
      MONGODB_URI: mongodb://mongodb:27017/product_service

  order-service:
    build: business-services/order-service
    ports:
      - "8083:8083"
    environment:
      EUREKA_DEFAULT_ZONE: http://discovery-server:8761/eureka
      ORDER_DATASOURCE_URL: jdbc:mysql://mysql:3306/order_service

  inventory-service:
    build: business-services/inventory-service
    ports:
      - "8082:8082"
    environment:
      EUREKA_DEFAULT_ZONE: http://discovery-server:8761/eureka
      INVENTORY_DATASOURCE_URL: jdbc:mysql://mysql:3306/inventory_service

  notification-service:
    build: business-services/notification-service
    ports:
      - "8084:8084"
    environment:
      EUREKA_DEFAULT_ZONE: http://discovery-server:8761/eureka
      NOTIFICATION_DATASOURCE_URL: jdbc:mysql://mysql:3306/notification_service

  api-gateway:
    build: infrastructure-services/api-gateway
    ports:
      - "8080:8080"
    environment:
      EUREKA_DEFAULT_ZONE: http://discovery-server:8761/eureka

volumes:
  mysql-data:
  mongo-data:
```

### 3. Build and Run Docker Compose

```bash
# Build all images
docker-compose build

# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

---

## Troubleshooting

### Issue: Services can't connect to databases
**Solution**: Check database credentials in application.yaml files match your setup

### Issue: Kafka topics not found
**Solution**: Create topics manually using kafka-topics command before starting services

### Issue: Service discovery not working
**Solution**: Ensure Eureka server is running on 8761 and eureka.client settings are correct

### Issue: Email not sending
**Solution**: Configure SMTP settings or use MailHog for local testing

### Issue: Feign client connection error
**Solution**: Verify inventory service is up and running before placing orders

---

## Performance Optimization

### 1. Enable Caching
Already implemented in Product Service for `getAllProducts()`

### 2. Connection Pooling
Configured in DataSource properties in application.yaml files

### 3. Async Processing
Kafka provides async event processing

### 4. Database Indexing
Add indexes to frequently queried columns:
```sql
CREATE INDEX idx_product_id ON inventory(product_id);
CREATE INDEX idx_status ON notifications(status);
CREATE INDEX idx_order_number ON orders(order_number);
```

---

## Monitoring

### Actuator Endpoints Available
- `/actuator/health` - Health check
- `/actuator/metrics` - Metrics
- `/actuator/info` - Service info
- `/actuator/prometheus` - Prometheus metrics

### Example Monitoring Setup
```bash
# Get service health
curl http://localhost:8081/actuator/health

# Get metrics
curl http://localhost:8081/actuator/metrics

# Get specific metric
curl http://localhost:8081/actuator/metrics/jvm.memory.used
```

---

## Next Steps

1. ✅ Complete build and compilation
2. ✅ Run integration tests
3. ✅ Deploy to Docker
4. ✅ Set up Kubernetes orchestration
5. ✅ Configure CI/CD pipeline
6. ✅ Implement API documentation (Swagger)
7. ✅ Set up centralized logging


