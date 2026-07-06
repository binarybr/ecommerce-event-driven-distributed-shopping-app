# ShopSphere

A **microservices e-commerce backend** in Spring Boot**
storefront — built to demonstrate distributed-systems patterns end-to-end: service
discovery, async event-driven flows, polyglot persistence, JWT-based RBAC, circuit
breakers, and Stripe-backed payments.

> **TL;DR:** browse → cart → checkout → real Stripe charge → email — across 13 services,
> two databases (MySQL + MongoDB), Kafka, and a reactive API gateway. One command to run.

---

## At a glance

| | |
|---|---|
| **Backend** | Java 25 · Spring Boot 3.x · Spring Cloud (Gateway, Eureka, Config, OpenFeign) · Spring Data JPA + MongoDB · Spring Security · Spring Kafka · Resilience4j |
| **Datastores** | MySQL 8 (transactional) · MongoDB 7 (catalog + text search) |
| **Messaging** | Apache Kafka (Confluent 7.6) · Zookeeper |
| **Payments / Email** | Stripe (test mode) · MailHog (dev SMTP) |
| **Ops** | Docker · Docker Compose · Maven (multi-module + wrapper) · `run.ps1` |

---

## Architecture

```
                  ┌─────────────────────────┐
  Browser / SPA ─▶│   API Gateway (8080)    │  single origin, no CORS
                  └─────────┬───────────────┘ routes /api/** (lb:// via Eureka)
                            │
   ┌──────────────────┬─────┴─────┬──────────────┬──────────────┬────────────┐
   ▼                  ▼           ▼              ▼              ▼            ▼
 user              product    inventory       order          payment       cart / review
 (MySQL)           (Mongo)    (MySQL)        (MySQL)        (MySQL)       / rec / admin
   │                  │           │              │              │
   └────── JWT validated locally by every service ──────────────┘
                            │
                            └──── Kafka events ────▶ notification-service ─▶ MailHog
```

## 🏗️ High-Level Architecture

```
                          ┌──────────────────────┐
                          │   EXTERNAL CLIENTS    │
                          └───────────┬──────────-┘
                                      │ HTTPS / Bearer JWT
                                      ▼
                          ┌──────────────────────┐
                          │     API GATEWAY       │  reactive (WebFlux)
                          │     (Port 8080)       │  routes /api/** via lb://
                          └───────────┬──────────-┘
                                      │  (service discovery)
        ┌───────────────┬────────────┼─────────────┬───────────────┬──────────────┐
        ▼               ▼            ▼             ▼               ▼              ▼
  ┌──────────┐   ┌──────────┐  ┌──────────┐  ┌──────────┐   ┌──────────┐  ┌──────────┐
  │  USER    │   │ PRODUCT  │  │INVENTORY │  │  ORDER   │   │ PAYMENT  │  │  CART    │
  │  8085    │   │  8081    │  │  8082    │  │  8083    │   │  8086    │  │  8087    │
  │  MySQL   │   │ MongoDB  │  │  MySQL   │  │  MySQL   │   │  MySQL   │  │  MySQL   │
  └──────────┘   └──────────┘  └──────────┘  └────┬─────┘   └──────────┘  └────┬─────┘
        ▼               ▼            ▼             │ Feign        ▼              │ Feign
  ┌──────────┐   ┌──────────┐  ┌──────────────┐   │ (reserve)┌──────────┐       │ (price)
  │NOTIFICATN│   │  REVIEW  │  │RECOMMENDATION│   └────────► │INVENTORY │       └──► PRODUCT
  │  8084    │   │  8088    │  │  8089        │              └──────────┘
  │  MySQL   │   │  MySQL   │  │  MySQL       │       ┌──────────┐
  └──────────┘   └──────────┘  └──────────────┘      │  ADMIN   │  aggregator (no DB)
                                                     │  8090    │  Feign → all services
                                                     └──────────┘

  Infrastructure: DISCOVERY (Eureka 8761) · CONFIG (8888) · KAFKA (9092) · MailHog (1025/8025)
```

**13 services in total:**
3 infrastructure (discovery, config, gateway) + 10 business (user, product, inventory,
order, payment, cart, review, recommendation, notification, admin).

### Key design choices
- **Polyglot persistence** — MongoDB for catalog/search (flexible schema, text indexes);
  MySQL for transactional order/payment/inventory data.
- **Sync vs async by purpose** — Feign for "need an answer now" (reserve stock, fetch
  price); Kafka for "fire and forget" (order events, email, status updates).
- **Stateless JWT** signed by user-service, validated locally everywhere — no auth
  callbacks.
- **Server-authoritative pricing** — order totals are recomputed from the catalog;
  client-supplied price is ignored.
- **Stripe idempotency keys** scoped to `userId-orderId` — retries never double-charge.
- **Optimistic locking** (`@Version`) on inventory — two customers can't both buy the
  last unit.
- **Resilience4j circuit breaker** around order → inventory, tuned to ignore *business*
  exceptions so "out of stock" doesn't trip the breaker.
- **Persistent Docker volumes** for both databases — data survives `down`/`up`.

---

## Repository layout

```
e-commerce-event-driven-shopping-app/
├── infrastructure-services/
│   ├── api-gateway/              # 8080 — Spring Cloud Gateway (reactive)
│   ├── discovery-server/         # 8761 — Netflix Eureka
│   └── config-server/            # 8888 — Spring Cloud Config
├── business-services/
│   ├── user-service/             # 8085 — auth, JWT, accounts (MySQL)
│   ├── product-service/          # 8081 — catalog + search (MongoDB)
│   ├── inventory-service/        # 8082 — stock + reservations (MySQL)
│   ├── order-service/            # 8083 — orders, lifecycle (MySQL, Resilience4j)
│   ├── payment-service/          # 8086 — Stripe charges (MySQL)
│   ├── cart-service/             # 8087 — per-user cart (MySQL)
│   ├── review-service/           # 8088 — reviews + ratings (MySQL)
│   ├── recommendation-service/   # 8089 — co-purchase / trending (MySQL)
│   ├── notification-service/     # 8084 — Kafka → SMTP → MailHog
│   └── admin-service/            # 8090 — Feign aggregator (admin dashboard)
├── common-library/               # Shared Kafka event DTOs
├── deployment/docker/            # docker-compose.yml, init scripts, .env
├── pom.xml                       # Maven multi-module parent
└── mvnw / mvnw.cmd               # Maven wrapper
```

Frontend (separate repo / folder): `e-commerce-event-driven-shopping-app-frontend/`.

---

## Running it

### Prerequisites
- **Docker Desktop** (running)
- **Java 25** (or the project JDK)
- A **Stripe test key** (`sk_test_…`) — get one free at https://dashboard.stripe.com/test/apikeys

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

> **Why the script:** the service Dockerfiles `COPY target/*.jar` — they do **not**
> compile inside Docker. Always run `mvnw clean package` before
> `docker compose ... up --build`. `run.ps1` handles this for you.

### Where things are

| URL | What |
|---|---|
| http://localhost:3000 | Frontend (storefront + admin dashboard) |
| http://localhost:8080 | API Gateway |
| http://localhost:8761 | Eureka dashboard |
| http://localhost:8025 | MailHog (captured emails) |
| `localhost:3307` | MySQL (root / `binary777Code`) |
| `localhost:27018` | MongoDB |

### Default logins

| Account | Email | Password |
|---|---|---|
| Pre-seeded admin | `admin@shopsphere.com` | `Admin@1234` |
| New admin via UI | (register → Admin card → enter key) | key: `ShopSphere@Admin2024` |
| New customer | register via UI | – |

### Smoke test the happy path
1. Log in as the seeded admin → confirm 12 demo products on the home page.
2. Add to cart → checkout → leave the Stripe token as `tok_visa` → see
   **"Payment succeeded"**.
3. Open **MailHog** (port 8025) → see the order confirmation email.
4. Check the order in your **Stripe dashboard → Payments** (test mode).

---
---

## Highlights (what's genuinely interesting in this codebase)

- **Reserve-then-confirm order flow** — synchronous stock reservation + asynchronous
  status transition over Kafka, with the circuit breaker correctly distinguishing
  business failures from infrastructure failures.
- **Idempotent Stripe payments** — `Idempotency-Key` HTTP header (not the body — a
  subtle pitfall) scoped to `userId-orderId`.
- **Server-side price validation** — orders ignore the client's price and recompute
  it from the catalog via Feign, closing a forged-price vulnerability.
- **Auto-seeded inventory** — when a product is created (admin form or DataSeeder),
  a Kafka `product-created` event triggers inventory-service to create an orderable
  stock row immediately.
- **Single-origin SPA** — multi-stage Docker (Node build → nginx) where nginx proxies
  `/api/` to the gateway, so the browser sees one origin and there's no CORS in prod.

---

## Stop / Reset

```powershell
# Stop everything, keep data
docker compose -f deployment\docker\docker-compose.yml down
cd "C:\Binary Labyrinth\VSCode Workspace\React Workspace\e-commerce-event-driven-shopping-app-frontend"
docker compose down
```
