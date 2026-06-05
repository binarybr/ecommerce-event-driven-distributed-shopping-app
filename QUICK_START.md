# 🚀 Quick Start Guide

> **Fastest path:** `docker compose -f deployment/docker/docker-compose.yml up -d`
> brings up all **13 services** + Kafka/MySQL/MongoDB/MailHog in one command
> (18 containers). The manual per-terminal steps below are for understanding/
> debugging individual services. For the full local guide + a 14-section
> regression script, see **RUNNING_LOCAL_GUIDE.md**.
>
> **Auth model:** product/review **reads are public**; everything else needs a
> **JWT Bearer** token from `POST /api/users/login`. The `admin/change-me`
> Basic credentials only guard the gateway's actuator endpoints.

## Prerequisites

- Java 25+ (or adjust `java.version` in pom.xml)
- Maven 3.8+
- MySQL 8.0+
- MongoDB 5.0+
- Kafka 3.5+ with Zookeeper
- Git

---

## 1️⃣ Database Setup

### Create MySQL Databases

```bash
# Using MySQL command line
mysql -u root -p

# Run SQL commands — 8 databases (one per MySQL-backed service)
CREATE DATABASE order_service          CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE inventory_service      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE notification_service   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE user_service           CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE payment_service        CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE cart_service           CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE review_service         CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE recommendation_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
# (product-service uses MongoDB, not MySQL)

# Or use the init script (creates all of the above)
mysql -u root -p < deployment/docker/mysql/init-databases.sql
```

### Start MongoDB

```bash
# macOS
brew services start mongodb-community

# Windows (assuming MongoDB is installed)
# Start MongoDB service from Services panel
# Or run: mongod --dbpath "C:\data\db"

# Linux
sudo systemctl start mongodb
```

---

## 2️⃣ Start Infrastructure Services

### Terminal 1: Discovery Server (Eureka)

```bash
cd infrastructure-services/discovery-server
mvn spring-boot:run
```

Expected output:
```
Tomcat started on port(s): 8761
Discovery Server is running...
```

Access: http://localhost:8761

---

### Terminal 2: Config Server

```bash
cd infrastructure-services/config-server
mvn spring-boot:run
```

Expected output:
```
Tomcat started on port(s): 8888
Config Server is running...
```

---

### Terminal 3: API Gateway

```bash
cd infrastructure-services/api-gateway
mvn spring-boot:run
```

Expected output:
```
Tomcat started on port(s): 8080
API Gateway is running...
```

Access: http://localhost:8080

---

## 3️⃣ Start Kafka & Zookeeper

### Terminal 4: Zookeeper

```bash
# macOS/Linux
zookeeper-server-start.sh config/zookeeper.properties

# Windows
zkServer.cmd
```

### Terminal 5: Kafka Broker

```bash
# macOS/Linux
kafka-server-start.sh config/server.properties

# Windows (adjust path to Kafka installation)
kafka-server-start.bat .\config\server.properties
```

Expected output:
```
[2026-05-18 23:50:00,000] INFO [SocketServer], Started data producers
```

---

## 4️⃣ Start Business Services

### Terminal 6: Product Service

```bash
cd business-services/product-service
mvn spring-boot:run
```

Expected output:
```
Registering application PRODUCT-SERVICE with eureka with initial status: UP
Tomcat started on port(s): 8081
```

---

### Terminal 7: Order Service

```bash
cd business-services/order-service
mvn spring-boot:run
```

Expected output:
```
Registering application ORDER-SERVICE with eureka with initial status: UP
Tomcat started on port(s): 8083
```

---

### Terminal 8: Inventory Service

```bash
cd business-services/inventory-service
mvn spring-boot:run
```

Expected output:
```
Registering application INVENTORY-SERVICE with eureka with initial status: UP
Tomcat started on port(s): 8082
```

---

### Terminal 9: Notification Service

```bash
cd business-services/notification-service
mvn spring-boot:run
```

Expected output:
```
Registering application NOTIFICATION-SERVICE with eureka with initial status: UP
Tomcat started on port(s): 8084
```

---

## 5️⃣ Verify Services Are Running

### Check Eureka Dashboard

```bash
curl http://localhost:8761
```

All services should be registered and showing as UP. With the full stack
(Docker Compose) you'll see **11** entries:
- API-GATEWAY
- USER-SERVICE, PRODUCT-SERVICE, INVENTORY-SERVICE, ORDER-SERVICE
- PAYMENT-SERVICE, CART-SERVICE, NOTIFICATION-SERVICE
- REVIEW-SERVICE, RECOMMENDATION-SERVICE, ADMIN-SERVICE

(If you only started the four services in the terminals above, you'll see those four + the gateway.)

---

## 6️⃣ Test the Application

> **Auth reminder:** reads below (GET products) are public. **Create/update**
> product & inventory require an **ADMIN** JWT; placing orders requires a
> CUSTOMER/ADMIN JWT. Get a token via `POST /api/users/login` and send it as
> `-H "Authorization: Bearer <token>"`. (Promote a user to ADMIN with:
> `UPDATE users SET role='ADMIN' WHERE email='...';`)

### Create a Product (ADMIN token required)

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <ADMIN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "category": "Electronics"
  }'
```

Expected Response:
```json
{
  "id": "507f1f77bcf86cd799439011",
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "createdAt": "2026-05-18T23:50:00"
}
```

### Add Inventory

```bash
curl -X POST http://localhost:8080/api/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "507f1f77bcf86cd799439011",
    "quantity": 100
  }'
```

### Check Inventory

```bash
curl http://localhost:8080/api/inventory?productId=507f1f77bcf86cd799439011&quantity=5
```

Expected Response:
```json
{
  "inStock": true,
  "availableQuantity": 100
}
```

### Place an Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "507f1f77bcf86cd799439011",
    "quantity": 2,
    "price": 1999.98
  }'
```

Expected Response:
```json
{
  "id": 1,
  "orderNumber": "ORD-550e8400-e29b-41d4-a716-446655440000",
  "productId": "507f1f77bcf86cd799439011",
  "quantity": 2,
  "price": 1999.98,
  "createdAt": "2026-05-18T23:50:00"
}
```

### Verify Notification Was Sent

```bash
curl http://localhost:8080/api/notifications
```

You should see a notification record with:
- Type: EMAIL
- Status: SENT
- Subject: "Order Placed Successfully"

---

## 🐳 Docker Deployment (Alternative)

If you prefer to use Docker Compose instead of manual setup:

```bash
# Build all services
mvn clean package -DskipTests

# Start all services with Docker Compose
docker-compose -f deployment/docker/docker-compose.yml up -d

# View logs
docker-compose -f deployment/docker/docker-compose.yml logs -f

# Stop all services
docker-compose -f deployment/docker/docker-compose.yml down
```

---

## 🔍 Debugging & Troubleshooting

### Service won't start?

1. Check port availability:
```bash
# macOS/Linux
lsof -i :8080  # Check if port 8080 is in use

# Windows
netstat -ano | findstr :8080
```

2. Check database connection:
```bash
# MySQL
mysql -h localhost -u root -p

# MongoDB
mongo --host localhost:27017
```

3. Check logs:
```bash
# Check specific service logs
tail -f business-services/product-service/target/*.log
```

### Services can't communicate?

1. Verify Eureka dashboard: http://localhost:8761
2. Check if all services are registered as UP
3. Check network/firewall settings
4. Verify Kafka is running: `jps | grep Kafka`

### Kafka issues?

```bash
# Check if Kafka topics exist
kafka-topics.sh --list --bootstrap-server localhost:9092

# Create required topics
kafka-topics.sh --create --topic order-placed --bootstrap-server localhost:9092
kafka-topics.sh --create --topic inventory-reserved --bootstrap-server localhost:9092
kafka-topics.sh --create --topic inventory-failed --bootstrap-server localhost:9092
kafka-topics.sh --create --topic product-created --bootstrap-server localhost:9092
```

---

## 📊 Monitoring Services

### Eureka Dashboard
```
http://localhost:8761/
```

Shows all registered services and their status.

### Actuator Health Endpoints

```bash
# API Gateway health
curl http://localhost:8080/actuator/health

# Product Service health
curl http://localhost:8081/actuator/health

# Order Service health
curl http://localhost:8083/actuator/health

# Inventory Service health
curl http://localhost:8082/actuator/health

# Notification Service health
curl http://localhost:8084/actuator/health
```

---

## 🛑 Stopping Services

Stop services in reverse order:

1. Stop Notification Service (Terminal 9)
2. Stop Inventory Service (Terminal 8)
3. Stop Order Service (Terminal 7)
4. Stop Product Service (Terminal 6)
5. Stop API Gateway (Terminal 3)
6. Stop Config Server (Terminal 2)
7. Stop Discovery Server (Terminal 1)
8. Stop Kafka (Terminal 5)
9. Stop Zookeeper (Terminal 4)

Use `Ctrl+C` in each terminal to stop the service.

---

## 📝 Default Credentials

| Purpose | Username | Password |
|---------|----------|----------|
| Gateway **actuator only** (not `/api/**`) | admin | change-me (Docker) / admin (local) |
| `/api/**` access | — | JWT Bearer from `POST /api/users/login` |
| Eureka | N/A | N/A |
| Config Server | N/A | N/A |
| MySQL | root | binary777Code (Docker) |

---

## 🎯 Common Tasks

### View all products
```bash
curl http://localhost:8080/api/products
```

### View a specific product
```bash
curl http://localhost:8080/api/products/{id}
```

### Update a product
```bash
curl -X PUT http://localhost:8080/api/products/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Product",
    "description": "Updated description",
    "price": 299.99
  }'
```

### Delete a product
```bash
curl -X DELETE http://localhost:8080/api/products/{id}
```

### Get all orders
```bash
curl http://localhost:8080/api/orders
```

### Get all notifications
```bash
curl http://localhost:8080/api/notifications
```

---

## 🆘 Need Help?

1. **Check logs**: Look at service output in each terminal
2. **Read documentation**: See RUNNING_LOCAL_GUIDE.md for the full local guide + regression script
3. **Review architecture**: See ARCHITECTURE_DIAGRAM.md for system overview
4. **Check project status**: See PROJECT_SUMMARY.md for current status; per-service detail in `business-services/<svc>/IMPLEMENTATION.md`

---

**Last Updated:** 2026-05-18  
**Status:** ✅ Ready to use

