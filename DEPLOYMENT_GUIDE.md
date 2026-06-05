# Deployment Configuration Guide

## Overview

This guide provides comprehensive documentation for deploying the Online Shopping Microservices application using different deployment methods: Docker Compose, Kubernetes, and local development setup.

> **Stack:** 13 services — 3 infrastructure (discovery 8761, config 8888,
> gateway 8080) + 10 business (user 8085, product 8081, inventory 8082,
> order 8083, payment 8086, cart 8087, notification 8084, review 8088,
> recommendation 8089, admin 8090) — plus Kafka, Zookeeper, MySQL, MongoDB,
> MailHog (18 containers total).
>
> **⚠️ Auth model:** `/api/**` is **not** protected by HTTP Basic at the
> gateway. Product/review **reads are public**; all other calls require a
> **JWT Bearer** token from `POST /api/users/login` (writes to product/
> inventory and `/api/admin/**` require an **ADMIN** token). The
> `admin:change-me` Basic credentials below apply **only** to the gateway's
> own `/actuator/**` endpoints. Treat any `-u admin:change-me` example against
> `/api/**` in this guide as illustrative of the actuator path only.

## Table of Contents

1. [Docker Compose Deployment](#docker-compose-deployment)
2. [Local Development Setup](#local-development-setup)
3. [Kubernetes Deployment](#kubernetes-deployment)
4. [Configuration Reference](#configuration-reference)
5. [Troubleshooting](#troubleshooting)
6. [Database Schema](#database-schema)
7. [Port Reference](#port-reference)

---

## Docker Compose Deployment

### Quick Start

The easiest way to run the entire application stack is using Docker Compose.

#### Prerequisites

- Docker & Docker Compose installed
- 4GB+ RAM available
- Port 8080, 8761, 8888 available

#### Build All Services

```bash
# Build all Docker images
cd online-shopping-app
mvn clean package  # Build all JARs first
docker-compose -f deployment/docker/docker-compose.yml build
```

#### Start All Services

```bash
# Start entire stack in background
docker-compose -f deployment/docker/docker-compose.yml up -d

# View logs
docker-compose -f deployment/docker/docker-compose.yml logs -f

# View specific service logs
docker-compose -f deployment/docker/docker-compose.yml logs -f product-service
```

#### Verify Services Are Running

```bash
# Check service status
docker-compose -f deployment/docker/docker-compose.yml ps

# Expected output:
# NAME                 STATUS              PORTS
# discovery-server     Up 2 minutes        0.0.0.0:8761->8761/tcp
# config-server        Up 2 minutes        0.0.0.0:8888->8888/tcp
# api-gateway          Up 2 minutes        0.0.0.0:8080->8080/tcp
# mysql               Up 2 minutes        0.0.0.0:3307->3306/tcp
# mongo               Up 2 minutes        0.0.0.0:27017->27017/tcp
# kafka               Up 2 minutes        0.0.0.0:9092->9092/tcp
# zookeeper           Up 2 minutes        0.0.0.0:2181->2181/tcp
# mailhog             Up 2 minutes        0.0.0.0:1025->1025/tcp, 0.0.0.0:8025->8025/tcp
# user-service        Up 2 minutes        (internal :8085)
# product-service     Up 2 minutes        (internal :8081)
# inventory-service   Up 2 minutes        (internal :8082)
# order-service       Up 2 minutes        (internal :8083)
# payment-service     Up 2 minutes        (internal :8086)
# cart-service        Up 2 minutes        (internal :8087)
# notification-service Up 2 minutes       (internal :8084)
# review-service      Up 2 minutes        (internal :8088)
# recommendation-service Up 2 minutes     (internal :8089)
# admin-service       Up 2 minutes        (internal :8090)
```

#### Access the Application

```bash
# Eureka Dashboard (service registry)
http://localhost:8761

# Config Server
http://localhost:8888

# MailHog Email UI (for testing notifications)
http://localhost:8025

# Gateway actuator health (Basic auth = actuator only)
curl http://localhost:8080/actuator/health -u admin:change-me

# Public API read (NO token needed)
curl http://localhost:8080/api/products

# Authenticated API call (JWT Bearer from /api/users/login)
curl http://localhost:8080/api/orders -H "Authorization: Bearer <JWT>"
```

#### Stop All Services

```bash
# Stop all services
docker-compose -f deployment/docker/docker-compose.yml down

# Stop and remove volumes (clean database)
docker-compose -f deployment/docker/docker-compose.yml down -v
```

### Docker Compose Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Network (microservices-net)        │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────┐      ┌──────────────┐                   │
│  │ Discovery      │      │ Config       │                   │
│  │ Server         │      │ Server       │                   │
│  │ (8761)         │      │ (8888)       │                   │
│  └────────────────┘      └──────────────┘                   │
│           │                      │                          │
│           └──────────┬───────────┘                          │
│                      │                                      │
│            ┌─────────▼─────────┐                           │
│            │   API Gateway     │                           │
│            │   (8080 - Main)   │                           │
│            └─────────┬─────────┘                           │
│                      │                                      │
│     ┌────────────────┼────────────────┐                    │
│     │                │                │                    │
│  ┌──▼──┐  ┌──────┐  ┌──▼──┐  ┌──────────┐               │
│  │Prod │  │Order │  │Inv  │  │Notif     │               │
│  │Svc  │  │Svc   │  │Svc  │  │Svc       │               │
│  │8081 │  │8083  │  │8082 │  │8084      │               │
│  └──┬──┘  └──┬───┘  └──┬──┘  └────┬─────┘               │
│     │       │         │          │                        │
│  ┌──▼───────▼─────────▼────────┐┌──▼──────────┐         │
│  │      MySQL                  ││  MongoDB    │         │
│  │  (order, inventory, notif)  ││ (products)  │         │
│  └─────────────────────────────┘└─────────────┘         │
│                                                             │
│  ┌──────────────────────────────────────┐               │
│  │  Kafka (Message Broker)              │               │
│  │  Zookeeper (Coordination)            │               │
│  │  MailHog (Email Testing)             │               │
│  └──────────────────────────────────────┘               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Local Development Setup

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- MySQL 8.0+
- MongoDB 7.0+
- Kafka 3.5+
- Apache Zookeeper 3.8+

### Start Each Service Individually

#### 1. Start Databases

```bash
# Start MySQL
mysql -u root -p < deployment/docker/mysql/init-databases.sql

# Start MongoDB
mongod --dbpath /path/to/mongodb

# Start Zookeeper
zkServer.sh start

# Start Kafka
kafka-server-start.sh config/server.properties
```

#### 2. Start Infrastructure Services

```bash
# Terminal 1: Start Discovery Server
cd infrastructure-services/discovery-server
mvn spring-boot:run

# Terminal 2: Start Config Server
cd infrastructure-services/config-server
mvn spring-boot:run

# Terminal 3: Start API Gateway
cd infrastructure-services/api-gateway
mvn spring-boot:run
```

#### 3. Start Business Services

```bash
# Terminal 4: Start Product Service
cd business-services/product-service
mvn spring-boot:run

# Terminal 5: Start Order Service
cd business-services/order-service
mvn spring-boot:run

# Terminal 6: Start Inventory Service
cd business-services/inventory-service
mvn spring-boot:run

# Terminal 7: Start Notification Service
cd business-services/notification-service
mvn spring-boot:run
```

### Configuration for Local Development

Create `application-dev.yaml` for each service:

```yaml
# Product Service
spring:
  application:
    name: product-service
  data:
    mongodb:
      uri: mongodb://localhost:27017/product_service
  kafka:
    bootstrap-servers: localhost:9092

server:
  port: 8081

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

---

## Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (1.24+)
- kubectl configured
- Docker images pushed to registry

### Deploy to Kubernetes

```bash
# Navigate to deployment module
cd deployment/kubernetes

# Create namespace
kubectl apply -f base/namespace.yaml

# Create ConfigMap and Secrets
kubectl apply -f base/app-config.yaml
kubectl create secret generic app-secrets \
  --from-literal=GATEWAY_USERNAME=admin \
  --from-literal=GATEWAY_PASSWORD=your-secure-password \
  -n online-shopping

# Deploy all services
kubectl apply -f base/

# Check deployment status
kubectl get deployments -n online-shopping
kubectl get services -n online-shopping
kubectl get pods -n online-shopping

# View logs
kubectl logs -f deployment/product-service -n online-shopping

# Port forward to access
kubectl port-forward svc/api-gateway 8080:8080 -n online-shopping
```

### Kubernetes Architecture

```
Namespace: online-shopping
├── Deployments (with 2 replicas each)
│   ├── discovery-server
│   ├── config-server
│   ├── api-gateway
│   ├── product-service
│   ├── order-service
│   ├── inventory-service
│   └── notification-service
├── Services
│   ├── discovery-server (ClusterIP:8761)
│   ├── config-server (ClusterIP:8888)
│   ├── api-gateway (LoadBalancer:8080)
│   ├── product-service (ClusterIP:8081)
│   ├── order-service (ClusterIP:8083)
│   ├── inventory-service (ClusterIP:8082)
│   └── notification-service (ClusterIP:8084)
├── StatefulSets
│   ├── mysql
│   ├── mongodb
│   └── kafka
└── ConfigMaps & Secrets
```

---

## Configuration Reference

### Environment Variables

#### API Gateway

```bash
GATEWAY_USERNAME=admin           # HTTP Basic Auth username
GATEWAY_PASSWORD=change-me       # HTTP Basic Auth password
EUREKA_DEFAULT_ZONE=...         # Discovery Server address
```

#### Product Service

```bash
MONGODB_URI=mongodb://...        # MongoDB connection string
KAFKA_BOOTSTRAP_SERVERS=...      # Kafka brokers
EUREKA_DEFAULT_ZONE=...         # Discovery Server address
```

#### Order Service

```bash
ORDER_DATASOURCE_URL=...         # MySQL connection
ORDER_DATASOURCE_USERNAME=root   # MySQL user
ORDER_DATASOURCE_PASSWORD=...    # MySQL password
KAFKA_BOOTSTRAP_SERVERS=...      # Kafka brokers
EUREKA_DEFAULT_ZONE=...         # Discovery Server address
```

#### Inventory Service

```bash
INVENTORY_DATASOURCE_URL=...     # MySQL connection
INVENTORY_DATASOURCE_USERNAME=root
INVENTORY_DATASOURCE_PASSWORD=...
KAFKA_BOOTSTRAP_SERVERS=...      # Kafka brokers
EUREKA_DEFAULT_ZONE=...         # Discovery Server address
```

#### Notification Service

```bash
NOTIFICATION_DATASOURCE_URL=...  # MySQL connection
NOTIFICATION_DATASOURCE_USERNAME=root
NOTIFICATION_DATASOURCE_PASSWORD=...
KAFKA_BOOTSTRAP_SERVERS=...      # Kafka brokers
MAIL_HOST=mailhog               # Email SMTP server
MAIL_PORT=1025                  # SMTP port
EUREKA_DEFAULT_ZONE=...         # Discovery Server address
```

---

## Troubleshooting

### Service Won't Start

1. **Check port availability:**
   ```bash
   # Linux/Mac
   lsof -i :8080
   
   # Windows
   netstat -ano | findstr :8080
   ```

2. **Check database connectivity:**
   ```bash
   # Test MySQL
   mysql -h localhost -u root -p
   
   # Test MongoDB
   mongo --host localhost:27017
   ```

3. **Check Kafka:**
   ```bash
   # Check Zookeeper
   echo ruok | nc localhost 2181
   ```

### Services Can't Communicate

1. **Verify Eureka registration:**
   - Visit http://localhost:8761
   - Check if all services are registered

2. **Check Docker network (Docker Compose):**
   ```bash
   docker network ls
   docker network inspect microservices-net
   ```

3. **Check service DNS resolution:**
   ```bash
   docker exec api-gateway nslookup product-service
   ```

### Database Connection Issues

1. **MySQL:**
   ```bash
   # Check if running
   docker ps | grep mysql
   
   # Connect to container
   docker exec -it mysql mysql -u root -p
   ```

2. **MongoDB:**
   ```bash
   # Check if running
   docker ps | grep mongo
   
   # Connect to container
   docker exec -it mongo mongo
   ```

---

## Database Schema

### MySQL Databases

#### order_service.orders

```sql
CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_number VARCHAR(36) UNIQUE NOT NULL,
  product_id VARCHAR(100) NOT NULL,
  quantity INT NOT NULL,
  price DOUBLE NOT NULL,
  status VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL
);

-- Indexes
CREATE INDEX idx_product_id ON orders(product_id);
CREATE INDEX idx_status ON orders(status);
CREATE INDEX idx_created_at ON orders(created_at);
```

#### inventory_service.inventory

```sql
CREATE TABLE inventory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id VARCHAR(100) UNIQUE NOT NULL,
  quantity INT NOT NULL
);

-- Indexes
CREATE INDEX idx_product_id ON inventory(product_id);
```

#### notification_service.notification

```sql
CREATE TABLE notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  recipient VARCHAR(255) NOT NULL,
  subject VARCHAR(255) NOT NULL,
  message LONGTEXT NOT NULL,
  type VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL,
  error_message VARCHAR(500),
  created_at DATETIME NOT NULL,
  sent_at DATETIME
);

-- Indexes
CREATE INDEX idx_recipient ON notification(recipient);
CREATE INDEX idx_status ON notification(status);
CREATE INDEX idx_created_at ON notification(created_at);
```

### MongoDB Collections

#### product_service.products

```javascript
db.products.createIndex({ "productCode": 1 }, { unique: true })
db.products.createIndex({ "name": "text" })
```

Sample document:
```json
{
  "_id": ObjectId("..."),
  "productCode": "PROD-001",
  "name": "Product Name",
  "description": "...",
  "price": 99.99,
  "category": "Electronics",
  "createdAt": ISODate("2024-01-15T10:30:00Z")
}
```

---

## Port Reference

| Service                | Port  | Purpose                          | Access Method        |
|------------------------|-------|----------------------------------|----------------------|
| API Gateway            | 8080  | Main external entry point        | http://localhost:8080 |
| Product Service        | 8081  | Catalog + full-text search       | Internal (via gateway) |
| Inventory Service      | 8082  | Stock management                 | Internal (via gateway) |
| Order Service          | 8083  | Order management                 | Internal (via gateway) |
| Notification Service   | 8084  | Email/SMS/push notifications     | Internal (via gateway) |
| User Service           | 8085  | Auth, JWT issuer, RBAC           | Internal (via gateway) |
| Payment Service        | 8086  | Stripe payments                  | Internal (via gateway) |
| Cart Service           | 8087  | Shopping cart                    | Internal (via gateway) |
| Review Service         | 8088  | Ratings & reviews                | Internal (via gateway) |
| Recommendation Service | 8089  | Co-purchase recommendations      | Internal (via gateway) |
| Admin Service          | 8090  | Admin dashboard (aggregator)     | Internal (via gateway) |
| Discovery Server       | 8761  | Eureka dashboard                 | http://localhost:8761 |
| Config Server          | 8888  | Configuration management         | http://localhost:8888 |
| MySQL                  | 3307  | Database (mapped from 3306)      | localhost:3307        |
| MongoDB                | 27017 | Document database                | localhost:27017       |
| Kafka                  | 9092  | Message broker                   | localhost:9092        |
| Zookeeper              | 2181  | Kafka coordination               | localhost:2181        |
| MailHog SMTP           | 1025  | Email capture (SMTP server)      | localhost:1025        |
| MailHog Web UI         | 8025  | Email testing UI                 | http://localhost:8025 |

---

## API Usage Examples

> Auth: get a JWT first via `POST /api/users/login` and pass it as
> `-H "Authorization: Bearer <JWT>"`. Product/inventory **writes** need an
> **ADMIN** token; placing orders needs a **CUSTOMER/ADMIN** token. Reads of
> the catalog are public.

### Login (get a token)

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}'
# → { "accessToken": "<JWT>", "user": { "id": 1, "role": "ADMIN", ... } }
```

### Create Product (ADMIN)

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <ADMIN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "price": 999.99,
    "description": "High-performance laptop",
    "category": "Electronics"
  }'
```

### Browse / Search (public, no token)

```bash
curl http://localhost:8080/api/products
curl "http://localhost:8080/api/products/search?q=laptop&sortBy=price&sortDir=ASC"
```

### Place Order (CUSTOMER/ADMIN)

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1",
    "productId": "<productId>",
    "quantity": 2,
    "price": 1999.98
  }'
```

### Add Inventory (ADMIN)

```bash
curl -X POST http://localhost:8080/api/inventory \
  -H "Authorization: Bearer <ADMIN_JWT>" \
  -H "Content-Type: application/json" \
  -d '{"productId":"<productId>","quantity":100}'
```

### Send Notification (CUSTOMER/ADMIN)

```bash
curl -X POST http://localhost:8080/api/notifications/email \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "customer@example.com",
    "subject": "Order Confirmation",
    "message": "Your order has been placed successfully"
  }'
```

---

## Performance Tuning

### Docker Compose
- Set JVM heap: `-e JAVA_OPTS="-Xmx1g -Xms512m"`
- Use volume mounts for faster file access

### Kubernetes
- Configure resource requests and limits
- Use horizontal pod autoscaling (HPA)
- Enable pod disruption budgets (PDB)

### Database
- Add appropriate indexes
- Configure connection pooling
- Monitor query performance

---

## Security Best Practices

1. **Change default credentials** in production
2. **Use HTTPS** for all communication
3. **Implement OAuth 2.0** for API authentication
4. **Rotate secrets** regularly
5. **Enable audit logging** for compliance
6. **Use network policies** to restrict traffic
7. **Implement rate limiting** to prevent abuse

---

## Monitoring and Logging

### View Logs

```bash
# Docker Compose
docker-compose logs -f [service-name]

# Kubernetes
kubectl logs -f deployment/[service-name] -n online-shopping

# Local development
tail -f ~/.spring-boot-devtools.log
```

### Health Checks

```bash
# All services
curl http://localhost:8080/actuator/health -u admin:change-me

# Specific endpoint
curl http://localhost:8080/actuator/metrics -u admin:change-me
```

---

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Microservices](https://spring.io/cloud)
- [Kafka Streams](https://kafka.apache.org/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Docker Documentation](https://docs.docker.com/)

---

Last Updated: January 2024

