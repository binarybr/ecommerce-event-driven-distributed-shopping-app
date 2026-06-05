# Running The Online Shopping App - Complete Local Setup Guide

This project is a **Spring Boot 4.0.6** multi-module microservices system with **7 business services**, **3 infrastructure services**, and multiple data stores (MySQL, MongoDB, Kafka, Zookeeper, MailHog).

## 📊 Project Architecture Overview

### Infrastructure Services (Required for all services)
| Service | Port | Purpose | Database |
|---------|------|---------|----------|
| Discovery Server (Eureka) | 8761 | Service registry and discovery | N/A |
| Config Server | 8888 | Centralized configuration | N/A |
| API Gateway | 8080 | Single entry point for all requests | N/A |

### Business Services (Core Application Logic)
| Service | Port | Purpose | Database | Message Queue |
|---------|------|---------|----------|----------------|
| Product Service | 8081 | Product catalog management | MongoDB | Kafka |
| Order Service | 8083 | Order placement and tracking | MySQL | Kafka |
| Inventory Service | 8082 | Stock management | MySQL | Kafka |
| Notification Service | 8084 | Email, SMS, Push notifications | MySQL | Kafka, MailHog |
| User Service | 8085 | User registration and authentication | MySQL | Kafka (JWT) |
| Payment Service | 8086 | Payment processing | MySQL | Kafka (Stripe) |
| Cart Service | 8087 | Shopping cart management | MySQL | Kafka |

### Infrastructure Components
- **MySQL 8.0**: Multi-database server (host port 3307 → container 3306)
- **MongoDB 7**: Document database for products (27017)
- **Kafka + Zookeeper**: Event streaming platform (9092, 2181)
- **MailHog**: Email testing service (1025 SMTP, 8025 Web UI)

---

## 📋 Prerequisites Installation

### Required Software

#### 1. Java 25
```powershell
# Download Java 25
# Option A: Oracle JDK
# https://www.oracle.com/java/technologies/downloads/#java25

# Option B: OpenJDK
# https://jdk.java.net/25

# Verify installation
java -version
# Expected: java version "25" or higher
```

#### 2. Maven (Included)
The project includes Maven wrapper at the root and in every module — no separate installation needed!
```powershell
# Verify Maven wrapper (from project root)
.\mvnw.cmd -version
# Expected: Apache Maven and Java 25+
```

#### 3. Docker Desktop
```powershell
# Download and install
# https://www.docker.com/products/docker-desktop

# Verify installation
docker --version
docker compose version
# Expected: Docker version X.XX.X and Docker Compose v2+
```

#### 4. Git (Optional but Recommended)
```powershell
# Download from
# https://git-scm.com/

# Verify
git --version
```

#### 5. kubectl (Optional - Only for Kubernetes)
```powershell
# Download from
# https://kubernetes.io/docs/tasks/tools/

# Verify
kubectl version --client
```

### System Requirements
- **Minimum RAM**: 8GB (16GB recommended for Docker Compose with all services)
- **Disk Space**: 10GB free space (includes Docker images, databases, Maven cache)
- **CPU**: Multi-core processor recommended
- **OS**: Windows 10/11, macOS, or Linux

### Optional Development Tools
- **IDE**: IntelliJ IDEA Community (free), VS Code, or Eclipse
- **REST Client**: Postman, Insomnia, or VS Code REST Client extension
- **Database GUI**: DBeaver, MySQL Workbench, or MongoDB Compass
- **Docker GUI**: Docker Desktop built-in or Portainer

---

## 🚀 Quick Start (5 minutes)

### For First-Time Users: Start Here

```powershell
# 1. Navigate to project root
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app"

# 2. Clean and build the entire project (must run before Docker build)
.\mvnw.cmd clean package -DskipTests

# 3. Start all services with Docker Compose
docker compose -f deployment\docker\docker-compose.yml up --build -d

# 4. Wait for all services to start (2-3 minutes)
# Monitor progress:
docker compose -f deployment\docker\docker-compose.yml logs -f

# 5. Verify all services are running
docker compose -f deployment\docker\docker-compose.yml ps

# 6. Test the API Gateway
curl.exe http://localhost:8080/actuator/health

# 7. Access the UIs
# - Eureka: http://localhost:8761
# - API Gateway: http://localhost:8080
# - MailHog: http://localhost:8025
```

---

## 📱 Building the Project

### Clean Build (Recommended)
```powershell
# Clean all compiled artifacts and rebuild
.\mvnw.cmd clean package -DskipTests

# Expected output:
# - Success message with "BUILD SUCCESS"
# - JAR files in each service's target/ directory
# - ~2-5 minutes depending on system speed

# Output location:
# - infrastructure-services/discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar
# - infrastructure-services/config-server/target/config-server-0.0.1-SNAPSHOT.jar
# - infrastructure-services/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
# - business-services/*/target/*.jar (for all business services)
```

### Build with Tests
```powershell
# Run all tests (slow but verifies correctness)
.\mvnw.cmd clean package

# Expected: BUILD SUCCESS with test results
```

### Build Single Service
```powershell
# Build only one service (and its dependencies)
.\mvnw.cmd clean package -DskipTests -pl business-services\product-service -am

# Flags explanation:
# -pl: Project List (specific module)
# -am: Also Make (build dependencies first)
```

### Skip Tests (Fast Build)
```powershell
# Fastest option - skip all tests
.\mvnw.cmd clean package -DskipTests
```

---

## 🐳 Running with Docker Compose (Recommended for Development)

### Prerequisites for Docker Compose
- ✅ Docker Desktop installed and running
- ✅ Project built first: `.\mvnw.cmd clean package -DskipTests`
- ✅ 8GB+ RAM available

> ⚠️ **Important**: Always run `.\mvnw.cmd clean package -DskipTests` before `docker compose up --build`.
> Docker copies the JAR from each service's `target/` folder — if the folder is missing or stale the build will fail.

### Start All Services

```powershell
# Navigate to project root
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app"

# Option 1: Start in foreground (see logs directly)
docker compose -f deployment\docker\docker-compose.yml up --build

# Option 2: Start in background (detached mode) - recommended
docker compose -f deployment\docker\docker-compose.yml up --build -d

# Option 3: Start without rebuilding images (after first run)
docker compose -f deployment\docker\docker-compose.yml up -d
```

### Monitor Services

```powershell
# Check all containers status
docker compose -f deployment\docker\docker-compose.yml ps
# Expected: UP status for all services

# View logs from all services
docker compose -f deployment\docker\docker-compose.yml logs -f

# View logs from specific service
docker compose -f deployment\docker\docker-compose.yml logs -f api-gateway

# View logs from multiple services
docker compose -f deployment\docker\docker-compose.yml logs -f api-gateway order-service

# Follow logs real-time (last 100 lines)
docker compose -f deployment\docker\docker-compose.yml logs -f --tail=100
```

### Verify Services Are Running

```powershell
# Check API Gateway is responding (no auth required for health)
curl.exe http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# List all containers
docker compose -f deployment\docker\docker-compose.yml ps

# Expected output should show all UP:
# - discovery-server
# - config-server
# - api-gateway
# - product-service
# - order-service
# - inventory-service
# - notification-service
# - user-service
# - payment-service
# - cart-service
# - mysql
# - mongo
# - kafka
# - zookeeper
# - mailhog
```

### Stop Services

```powershell
# Stop all services (keep data volumes)
docker compose -f deployment\docker\docker-compose.yml down

# Stop all services and remove volumes (resets all databases)
docker compose -f deployment\docker\docker-compose.yml down -v

# Stop and remove everything (clean slate for next run)
docker compose -f deployment\docker\docker-compose.yml down -v --remove-orphans
```

### Restart Individual Services

```powershell
# Restart a specific service
docker compose -f deployment\docker\docker-compose.yml restart api-gateway

# Restart multiple services
docker compose -f deployment\docker\docker-compose.yml restart order-service inventory-service

# Restart all services
docker compose -f deployment\docker\docker-compose.yml restart
```

### View Service Configuration

```powershell
# View resolved docker-compose config
docker compose -f deployment\docker\docker-compose.yml config

# Inspect a specific container
docker inspect <container-name>
```

---

## 💻 Running Services Locally (Without Docker)

### Prerequisites
- ✅ Java 25 installed
- ✅ MySQL running on localhost:3306 (your local MySQL)
- ✅ MongoDB running on localhost:27017 (your local MongoDB)
- ✅ Kafka + Zookeeper running on localhost:9092 (use Docker for this — see below)
- ✅ MailHog running (optional — for testing emails)

### Start Kafka via Docker (easiest option)

Even when running services locally, use Docker just for Kafka and MailHog:

```powershell
# Start only Kafka, Zookeeper and MailHog via Docker
docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:7.6.0 `
  -e ZOOKEEPER_CLIENT_PORT=2181

docker run -d --name kafka -p 9092:9092 confluentinc/cp-kafka:7.6.0 `
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 `
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 `
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1

docker run -d --name mailhog -p 1025:1025 -p 8025:8025 mailhog/mailhog:v1.0.1
```

### Step 1: Create MySQL Databases

Before starting any service, create all required databases in your local MySQL:

```sql
-- Run this in MySQL Workbench, DBeaver, or MySQL CLI
CREATE DATABASE IF NOT EXISTS order_service;
CREATE DATABASE IF NOT EXISTS inventory_service;
CREATE DATABASE IF NOT EXISTS notification_service;
CREATE DATABASE IF NOT EXISTS user_service;
CREATE DATABASE IF NOT EXISTS payment_service;
CREATE DATABASE IF NOT EXISTS cart_service;
```

```powershell
# Or run via command line (adjust password if different)
mysql -u root -pbinary777Code -e "
  CREATE DATABASE IF NOT EXISTS order_service;
  CREATE DATABASE IF NOT EXISTS inventory_service;
  CREATE DATABASE IF NOT EXISTS notification_service;
  CREATE DATABASE IF NOT EXISTS user_service;
  CREATE DATABASE IF NOT EXISTS payment_service;
  CREATE DATABASE IF NOT EXISTS cart_service;
"
```

### Step 2: Start Infrastructure Services

Open a **new terminal window** for each service and keep them all running.

#### Terminal 1 — Discovery Server
```powershell
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\infrastructure-services\discovery-server"
.\mvnw.cmd spring-boot:run
# Wait for: "Started DiscoveryServerApplication" on port 8761
```

#### Terminal 2 — Config Server
```powershell
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\infrastructure-services\config-server"
.\mvnw.cmd spring-boot:run
# Wait for: "Started ConfigServerApplication" on port 8888
```

#### Terminal 3 — API Gateway
```powershell
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\infrastructure-services\api-gateway"
.\mvnw.cmd spring-boot:run
# Wait for: "Started ApiGatewayApplication" on port 8080
```

### Step 3: Start Business Services (Each in a New Terminal)

```powershell
# Terminal 4 — Product Service
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\business-services\product-service"
.\mvnw.cmd spring-boot:run

# Terminal 5 — Inventory Service
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\business-services\inventory-service"
.\mvnw.cmd spring-boot:run

# Terminal 6 — Order Service
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\business-services\order-service"
.\mvnw.cmd spring-boot:run

# Terminal 7 — Notification Service
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\business-services\notification-service"
.\mvnw.cmd spring-boot:run

# Terminal 8 — User Service
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\business-services\user-service"
.\mvnw.cmd spring-boot:run

# Terminal 9 — Payment Service
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\business-services\payment-service"
.\mvnw.cmd spring-boot:run

# Terminal 10 — Cart Service
cd "C:\Binary Labyrinth\IntelliJ Workspace\online-shopping-app\business-services\cart-service"
.\mvnw.cmd spring-boot:run
```

### Step 4: Verify All Services Started

```powershell
# Open Eureka dashboard — should show 8 registered services
Start-Process "http://localhost:8761"

# Check API Gateway health
curl.exe http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

---

## 🔐 Authentication Reference

| Mode | Username | Password | Notes |
|------|----------|----------|-------|
| **Local run** (direct) | `admin` | `admin` | Default in `application.yaml` |
| **Docker Compose** | `admin` | `change-me` | Set via `GATEWAY_PASSWORD` env var |

> The API Gateway uses **HTTP Basic Auth**. All API requests through `localhost:8080` require this header.
> Individual service health endpoints (`/actuator/health`) do **not** require auth.

---

## 🧪 Testing the Application

### 1. Health Check Tests (No Auth Required)

```powershell
# API Gateway
curl.exe http://localhost:8080/actuator/health

# Direct service health checks (bypass gateway)
curl.exe http://localhost:8081/actuator/health  # Product Service
curl.exe http://localhost:8082/actuator/health  # Inventory Service
curl.exe http://localhost:8083/actuator/health  # Order Service
curl.exe http://localhost:8084/actuator/health  # Notification Service
curl.exe http://localhost:8085/actuator/health  # User Service
curl.exe http://localhost:8086/actuator/health  # Payment Service
curl.exe http://localhost:8087/actuator/health  # Cart Service
```

### 2. API Tests via API Gateway

> Use `admin:admin` for **local run**, `admin:change-me` for **Docker Compose**.
> Examples below use Docker Compose credentials.

#### Test 1: Register a User
```powershell
curl.exe -X POST http://localhost:8080/api/users/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890"
  }'

# Expected: 201 Created with user details and ID
# Note the returned "id" — you will need it for placing orders
```

#### Test 2: Login and Get JWT Token
```powershell
curl.exe -X POST http://localhost:8080/api/users/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'

# Expected: 200 OK with accessToken (JWT)
# Use this token for cart and payment endpoints
```

#### Test 3: Create a Product
```powershell
curl.exe -u admin:change-me -X POST http://localhost:8080/api/products `
  -H "Content-Type: application/json" `
  -d '{
    "name": "Mechanical Keyboard",
    "description": "RGB Mechanical Keyboard with Cherry MX switches",
    "price": 99.99
  }'

# Expected: 201 Created with product ID (MongoDB ObjectId)
# Note the returned "id" — you will need it for inventory and orders
```

#### Test 4: Add Inventory for the Product
```powershell
# Replace <productId> with the ID returned from Test 3
curl.exe -u admin:change-me -X POST http://localhost:8080/api/inventory `
  -H "Content-Type: application/json" `
  -d '{
    "productId": "<productId>",
    "quantity": 100
  }'

# Expected: 201 Created
```

#### Test 5: List Products
```powershell
curl.exe -u admin:change-me -X GET http://localhost:8080/api/products

# Expected: Array of products
```

#### Test 6: Place an Order
```powershell
# Replace <userId> with the user ID from Test 1
# Replace <productId> with the product ID from Test 3
curl.exe -u admin:change-me -X POST http://localhost:8080/api/orders `
  -H "Content-Type: application/json" `
  -d '{
    "userId": "<userId>",
    "productId": "<productId>",
    "quantity": 2,
    "price": 199.98
  }'

# Expected: 201 Created with order details and status "PLACED"
# This also triggers:
#   - Inventory reduction
#   - OrderPlacedEvent on Kafka
#   - Confirmation email via MailHog (http://localhost:8025)
```

#### Test 7: Check Order Status
```powershell
# Replace <orderId> with the ID from Test 6
curl.exe -u admin:change-me -X GET http://localhost:8080/api/orders/<orderId>

# Expected: Order with status "CONFIRMED" (updated by InventoryEventConsumer)
```

#### Test 8: Get Notifications
```powershell
curl.exe -u admin:change-me -X GET http://localhost:8080/api/notifications

# Expected: Array with order confirmation email notification (status: SENT)
```

#### Test 9: Check Email in MailHog
```powershell
# Open MailHog web UI
Start-Process "http://localhost:8025"

# You should see the order confirmation email sent by Notification Service
```

### 3. Automated Tests

```powershell
# Run all tests
.\mvnw.cmd clean test

# Run tests for a specific service
.\mvnw.cmd clean test -pl business-services\product-service

# Run a specific test class
.\mvnw.cmd clean test -Dtest=ProductServiceApplicationTests

# Run tests with verbose output
.\mvnw.cmd clean test -DforkMode=never
```

### 4. Kafka Topic Testing

```powershell
# List all Kafka topics (from inside the kafka container)
docker exec kafka kafka-topics --list --bootstrap-server kafka:9092

# Monitor a specific topic in real time
docker exec kafka kafka-console-consumer `
  --bootstrap-server kafka:9092 `
  --topic order-placed `
  --from-beginning

# Expected topics after running services:
# order-placed
# inventory-reserved
# inventory-failed
# user-registered
# payment-processed
# payment-failed
# payment-refunded
# item-added-to-cart
# cart-cleared
# product-created
```

### 5. Database Testing

```powershell
# ── MySQL ──────────────────────────────────────────────────────
# Connect to MySQL inside Docker
docker exec -it mysql mysql -u root -pbinary777Code

# List all databases (shows 4 system + 6 application databases)
SHOW DATABASES;

# Expected application databases:
# cart_service, inventory_service, notification_service,
# order_service, payment_service, user_service

# Query orders in order_service
USE order_service;
SELECT * FROM orders;

# Query inventory
USE inventory_service;
SELECT * FROM inventory;

# ── MongoDB ────────────────────────────────────────────────────
# Connect to MongoDB inside Docker
docker exec -it mongo mongosh

# List databases
show dbs

# Use product_service database
use product_service

# Query products (collection name is "products")
db.products.find().pretty()
```

### 6. Email Testing (MailHog)

```powershell
# Open MailHog web interface
Start-Process "http://localhost:8025"

# All emails sent by Notification Service appear here:
# - Order confirmation (triggered by placing an order)
# - Welcome email (triggered by user registration)
# - Payment confirmation (triggered by successful payment)
```

---

## 🐳 Running with Kubernetes

### Prerequisites
- ✅ Docker Desktop with Kubernetes enabled OR Minikube/Kind installed
- ✅ kubectl installed and configured
- ✅ Project built: `.\mvnw.cmd clean package -DskipTests`

### Build Docker Images

```powershell
# Build all service images (run from project root)
docker build -t online-shopping/discovery-server:0.0.1-SNAPSHOT  infrastructure-services\discovery-server
docker build -t online-shopping/config-server:0.0.1-SNAPSHOT     infrastructure-services\config-server
docker build -t online-shopping/api-gateway:0.0.1-SNAPSHOT       infrastructure-services\api-gateway
docker build -t online-shopping/product-service:0.0.1-SNAPSHOT   business-services\product-service
docker build -t online-shopping/order-service:0.0.1-SNAPSHOT     business-services\order-service
docker build -t online-shopping/inventory-service:0.0.1-SNAPSHOT business-services\inventory-service
docker build -t online-shopping/notification-service:0.0.1-SNAPSHOT business-services\notification-service
docker build -t online-shopping/user-service:0.0.1-SNAPSHOT      business-services\user-service
docker build -t online-shopping/payment-service:0.0.1-SNAPSHOT   business-services\payment-service
docker build -t online-shopping/cart-service:0.0.1-SNAPSHOT      business-services\cart-service
```

### For Minikube Users

```powershell
# Start Minikube
minikube start

# Point Docker CLI to Minikube's Docker daemon (run before building images)
minikube docker-env | Invoke-Expression

# Then build all images (same commands as above)
docker build -t online-shopping/discovery-server:0.0.1-SNAPSHOT infrastructure-services\discovery-server
# ... (build all other services)
```

### Deploy to Kubernetes

```powershell
# Validate manifests
kubectl kustomize deployment\kubernetes\base

# Apply the stack
kubectl apply -k deployment\kubernetes\base

# Check deployment status
kubectl get pods -n online-shopping
kubectl get svc -n online-shopping

# Watch rollout progress
kubectl rollout status deployment/api-gateway -n online-shopping

# View service ports
kubectl get svc -n online-shopping
```

### Monitor Kubernetes Deployment

```powershell
# View all pods
kubectl get pods -n online-shopping

# View pod logs
kubectl logs -f deployment/api-gateway -n online-shopping

# View service details
kubectl describe svc api-gateway -n online-shopping

# Port forward to access services locally
kubectl port-forward svc/api-gateway 8080:8080 -n online-shopping

# Port forward to Eureka
kubectl port-forward svc/discovery-server 8761:8761 -n online-shopping
```

### Clean Up Kubernetes

```powershell
# Delete all resources
kubectl delete -k deployment\kubernetes\base

# Or delete the whole namespace
kubectl delete namespace online-shopping
```

---

## 📍 Service URLs and Access

### API Gateway (Main Entry Point)
- **URL**: `http://localhost:8080`
- **Auth (Local)**: `admin` / `admin`
- **Auth (Docker)**: `admin` / `change-me`
- **Health** (no auth): `http://localhost:8080/actuator/health`

### Service Dashboards

| Service | URL | Purpose |
|---------|-----|---------|
| **Eureka Dashboard** | http://localhost:8761 | Service registry — see all registered services |
| **Config Server** | http://localhost:8888 | Configuration management |
| **MailHog** | http://localhost:8025 | Captured emails (SMTP tester) |

### Direct Service URLs (bypass gateway — no auth needed for health)

| Service | Port | Health URL |
|---------|------|-----------|
| Product Service | 8081 | http://localhost:8081/actuator/health |
| Inventory Service | 8082 | http://localhost:8082/actuator/health |
| Order Service | 8083 | http://localhost:8083/actuator/health |
| Notification Service | 8084 | http://localhost:8084/actuator/health |
| User Service | 8085 | http://localhost:8085/actuator/health |
| Payment Service | 8086 | http://localhost:8086/actuator/health |
| Cart Service | 8087 | http://localhost:8087/actuator/health |

### Database Access

| Database | Host | Port | Username | Password | Note |
|----------|------|------|----------|----------|------|
| MySQL | localhost | 3307 | root | binary777Code | Docker mapped port (host 3307 → container 3306) |
| MySQL | localhost | 3306 | root | binary777Code | Direct local MySQL (non-Docker) |
| MongoDB | localhost | 27017 | — | — | No auth in dev mode |
| Kafka | localhost | 9092 | — | — | Local / Docker external listener |

---

## 🔧 Troubleshooting

### Issue: "Port already in use"

```powershell
# Find process using port (e.g., 8080)
netstat -ano | findstr :8080

# Kill process (replace <PID> with actual process ID)
taskkill /PID <PID> /F

# Or change the port in the service's application.yaml:
# server:
#   port: 9080
```

### Issue: Docker Compose services won't start

```powershell
# View logs for all services
docker compose -f deployment\docker\docker-compose.yml logs

# Most common cause: JAR not built yet — rebuild first
.\mvnw.cmd clean package -DskipTests
docker compose -f deployment\docker\docker-compose.yml up --build -d

# Check disk space
docker system df

# Clean up unused images and volumes
docker system prune -a -v
```

### Issue: MySQL connection fails

```powershell
# Check if MySQL container is running
docker compose -f deployment\docker\docker-compose.yml ps mysql

# Check MySQL logs
docker compose -f deployment\docker\docker-compose.yml logs mysql

# Connect directly to verify
docker exec -it mysql mysql -u root -pbinary777Code

# If init failed, reset the volume and restart
docker compose -f deployment\docker\docker-compose.yml down -v
docker compose -f deployment\docker\docker-compose.yml up -d mysql
# Wait ~30 seconds for initialization to complete
```

### Issue: Services not registering in Eureka

```powershell
# Check Discovery Server logs
docker compose -f deployment\docker\docker-compose.yml logs discovery-server

# Verify services can reach Discovery Server from inside Docker network
docker exec api-gateway curl http://discovery-server:8761/eureka/apps

# Restart affected services
docker compose -f deployment\docker\docker-compose.yml restart api-gateway order-service
```

### Issue: Out of Memory

```powershell
# Increase Docker memory in Docker Desktop
# Settings → Resources → Memory → set to 8GB or higher

# Check current resource usage
docker stats

# Stop less critical services to free memory
docker compose -f deployment\docker\docker-compose.yml stop payment-service cart-service user-service
```

### Issue: Services can't communicate

```powershell
# List Docker networks
docker network ls

# Inspect microservices network
docker network inspect microservices-net

# Check DNS resolution inside Docker network
docker exec api-gateway nslookup order-service

# Test connectivity from gateway to order service
docker exec api-gateway curl -v http://order-service:8083/actuator/health
```

### Issue: No emails appearing in MailHog

```powershell
# Check Notification Service logs
docker compose -f deployment\docker\docker-compose.yml logs notification-service

# Verify MailHog container is running
docker compose -f deployment\docker\docker-compose.yml ps mailhog

# Check notification records in MySQL
docker exec -it mysql mysql -u root -pbinary777Code -e "SELECT * FROM notification_service.notifications;"
```

### Issue: Test failures

```powershell
# Run tests with verbose output
.\mvnw.cmd clean test -X -DforkMode=never

# Run a specific test class
.\mvnw.cmd test -Dtest=OrderServiceApplicationTests

# Run tests for one module only
cd business-services\order-service
.\mvnw.cmd test
```

### Issue: "ImagePullBackOff" in Kubernetes

```powershell
# For Minikube: make sure you built images inside Minikube's Docker daemon
minikube docker-env | Invoke-Expression
docker build -t online-shopping/api-gateway:0.0.1-SNAPSHOT infrastructure-services\api-gateway

# For Kind: load images manually
kind load docker-image online-shopping/api-gateway:0.0.1-SNAPSHOT

# For external cluster: push to a registry and update manifests
docker tag online-shopping/api-gateway:0.0.1-SNAPSHOT myregistry.com/api-gateway:0.0.1-SNAPSHOT
docker push myregistry.com/api-gateway:0.0.1-SNAPSHOT
```

---

## 📊 Verification Checklist

### Local Setup
- [ ] Java 25 installed: `java -version`
- [ ] Maven working: `.\mvnw.cmd -version`
- [ ] Docker running: `docker ps`
- [ ] Project builds successfully: `.\mvnw.cmd clean package -DskipTests`

### Docker Compose Running
- [ ] All containers UP: `docker compose -f deployment\docker\docker-compose.yml ps`
- [ ] API Gateway responds: `curl.exe http://localhost:8080/actuator/health`
- [ ] Eureka shows services registered: `http://localhost:8761`
- [ ] MySQL has 6 custom application databases:
  ```powershell
  docker exec mysql mysql -u root -pbinary777Code -e "SHOW DATABASES;" | findstr "_service"
  # Expected: cart_service, inventory_service, notification_service,
  #           order_service, payment_service, user_service
  ```
- [ ] Kafka topics created:
  ```powershell
  docker exec kafka kafka-topics --list --bootstrap-server kafka:9092
  ```

### Application Tests
- [ ] Can register a user: `POST /api/users/register`
- [ ] Can login and receive JWT: `POST /api/users/login`
- [ ] Can create a product: `POST /api/products`
- [ ] Can add inventory: `POST /api/inventory`
- [ ] Can place an order: `POST /api/orders` (with `userId` field)
- [ ] Order status changes to CONFIRMED after placement
- [ ] Email appears in MailHog: `http://localhost:8025`
- [ ] Notifications saved in DB: `GET /api/notifications`

---

## 🚀 Performance Tips

### Optimize Docker Compose Performance

```powershell
# 1. Use BuildKit for faster image builds
$env:DOCKER_BUILDKIT=1
docker compose -f deployment\docker\docker-compose.yml build

# 2. Skip rebuild if images are already up to date
docker compose -f deployment\docker\docker-compose.yml up -d
# (omit --build flag after first run)
```

### Optimize Maven Builds

```powershell
# 1. Use offline mode if all dependencies are cached
.\mvnw.cmd clean package -DskipTests -o

# 2. Parallel build (1 thread per CPU core)
.\mvnw.cmd clean package -DskipTests -T 1C

# 3. Rebuild only changed module (resume from failure)
.\mvnw.cmd clean package -DskipTests -rf :user-service
```

### Monitor Resource Usage

```powershell
# Docker container stats (live)
docker stats

# Java process memory (PowerShell)
Get-Process | Where-Object {$_.Name -like "java"} |
  Format-Table Name, @{l="Memory(MB)";e={[math]::Round($_.WorkingSet/1MB,1)}} -AutoSize
```

---

## 📞 Getting Help

### Collect All Logs

```powershell
# Save all logs to a file for debugging
docker compose -f deployment\docker\docker-compose.yml logs > logs.txt

# Logs from specific container with timestamp
docker logs --timestamps <container-name>

# Last 5 minutes of logs
docker logs --since 5m <container-name>
```

### Quick Health Check Script

Save this as `check-health.ps1` in your project root:

```powershell
$checks = @(
    @{ Name="Discovery Server"; Url="http://localhost:8761/actuator/health" },
    @{ Name="Config Server";    Url="http://localhost:8888/actuator/health" },
    @{ Name="API Gateway";      Url="http://localhost:8080/actuator/health" },
    @{ Name="Product Service";  Url="http://localhost:8081/actuator/health" },
    @{ Name="Inventory Service";Url="http://localhost:8082/actuator/health" },
    @{ Name="Order Service";    Url="http://localhost:8083/actuator/health" },
    @{ Name="Notification Svc"; Url="http://localhost:8084/actuator/health" },
    @{ Name="User Service";     Url="http://localhost:8085/actuator/health" },
    @{ Name="Payment Service";  Url="http://localhost:8086/actuator/health" },
    @{ Name="Cart Service";     Url="http://localhost:8087/actuator/health" }
)

foreach ($check in $checks) {
    try {
        $response = Invoke-WebRequest -Uri $check.Url -UseBasicParsing -TimeoutSec 3
        Write-Host "✅  $($check.Name)" -ForegroundColor Green
    } catch {
        Write-Host "❌  $($check.Name) — UNREACHABLE" -ForegroundColor Red
    }
}
```

Run it:
```powershell
.\check-health.ps1
```

---

## 📖 Common Commands Reference

```powershell
# ── Docker Compose ─────────────────────────────────────────────
docker compose -f deployment\docker\docker-compose.yml up --build -d   # Build & start
docker compose -f deployment\docker\docker-compose.yml up -d            # Start (no rebuild)
docker compose -f deployment\docker\docker-compose.yml down             # Stop
docker compose -f deployment\docker\docker-compose.yml down -v          # Stop + wipe data
docker compose -f deployment\docker\docker-compose.yml restart          # Restart all
docker compose -f deployment\docker\docker-compose.yml logs -f          # Live logs
docker compose -f deployment\docker\docker-compose.yml ps               # Status

# ── Maven ──────────────────────────────────────────────────────
.\mvnw.cmd clean                          # Clean
.\mvnw.cmd compile                        # Compile only
.\mvnw.cmd package -DskipTests            # Build JARs
.\mvnw.cmd test                           # Run tests
.\mvnw.cmd install -DskipTests            # Install to local repo
.\mvnw.cmd package -DskipTests -T 1C      # Parallel build

# ── Kafka (inside Docker) ──────────────────────────────────────
docker exec kafka kafka-topics --list --bootstrap-server kafka:9092
docker exec kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic order-placed --from-beginning

# ── Kubernetes ─────────────────────────────────────────────────
kubectl apply -k deployment\kubernetes\base              # Deploy
kubectl get pods -n online-shopping                      # List pods
kubectl logs -f deployment/api-gateway -n online-shopping # Logs
kubectl delete -k deployment\kubernetes\base             # Delete

# ── API calls (Docker Compose — use admin:admin for local run) ─
curl.exe -u admin:change-me -X GET  http://localhost:8080/api/products
curl.exe -u admin:change-me -X POST http://localhost:8080/api/products `
  -H "Content-Type: application/json" `
  -d '{"name":"Test","description":"Test product","price":9.99}'

curl.exe -u admin:change-me -X POST http://localhost:8080/api/orders `
  -H "Content-Type: application/json" `
  -d '{"userId":"1","productId":"<productId>","quantity":1,"price":9.99}'
```

---

## 📚 Project Structure

```
online-shopping-app/
├── infrastructure-services/
│   ├── discovery-server/        # Eureka service registry       (port 8761)
│   ├── config-server/           # Centralized configuration     (port 8888)
│   └── api-gateway/             # Request router & load balancer(port 8080)
├── business-services/
│   ├── product-service/         # Product catalog               (port 8081)
│   ├── inventory-service/       # Stock management              (port 8082)
│   ├── order-service/           # Order management              (port 8083)
│   ├── notification-service/    # Email, SMS, Push              (port 8084)
│   ├── user-service/            # Users & JWT auth              (port 8085)
│   ├── payment-service/         # Payments (Stripe)             (port 8086)
│   └── cart-service/            # Shopping cart                 (port 8087)
├── common-library/              # Shared Kafka event DTOs
├── deployment/
│   ├── docker/
│   │   ├── docker-compose.yml
│   │   └── mysql/init-databases.sql
│   └── kubernetes/base/
├── pom.xml                      # Parent Maven configuration
└── RUNNING_LOCAL_GUIDE.md       # This file
```

---

## 🎓 Learning Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Apache Kafka Official](https://kafka.apache.org/)
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)

---

## ✅ Summary

This guide covers:
1. **Prerequisites** — Java 25, Maven wrapper, Docker Desktop
2. **Building** — `mvnw.cmd clean package -DskipTests` (always before Docker build)
3. **Docker Compose** — recommended for easy full-stack startup
4. **Local run** — infrastructure services are in `infrastructure-services/`, business in `business-services/`
5. **Kubernetes** — build images then `kubectl apply -k`
6. **Testing** — ordered API tests with correct payloads (including `userId` for orders)
7. **Troubleshooting** — common fixes for ports, MySQL, Kafka, memory

**Recommended approach**: Maven build → Docker Compose up → test via API Gateway.

---

**Last Updated**: May 20, 2026
**Project**: Online Shopping Microservices Application
**Status**: Ready for Local Development and Testing
