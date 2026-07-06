# ShopSphere

A **microservices e-commerce backend** in Spring Boot with a **React + TypeScript**
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
| **Frontend** | React 18 · TypeScript 5 · Vite 5 · React Router 6 · Axios · nginx |
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
├── docs/                         # Story, workflow, notes, interview Q&A
├── run.ps1                       # One-command build & run (Windows)
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

### First-time setup
```powershell
cd "C:\Binary Labyrinth\IntelliJ Workspace\e-commerce-event-driven-shopping-app"
copy deployment\docker\.env.example deployment\docker\.env
# Edit .env and paste your STRIPE_API_KEY=sk_test_...
```

### Run
```powershell
.\run.ps1                 # Maven build → backend → frontend → verify
.\run.ps1 -Clean          # wipe volumes and start completely fresh
.\run.ps1 -SkipBuild      # fast restart, reuse existing JARs
.\run.ps1 -SkipFrontend   # backend only
```

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

## Documentation

The `docs/` directory tells the full story:

| File | What's inside |
|---|---|
| [`docs/STORY.md`](./docs/STORY.md) | Narrative: business problem → outcomes → war stories → from-scratch blueprint |
| [`docs/WORKFLOW.md`](./docs/WORKFLOW.md) | System map: service catalogue, security model, flagship flows, Kafka topic map |
| [`docs/NOTES.md`](./docs/NOTES.md) | Component & feature reference with code examples |
| [`docs/services/`](./docs/services/) | Per-service deep dives (one MD per service) |
| [`docs/INTERVIEW_QA.md`](./docs/INTERVIEW_QA.md) | Interview prep — system design, war stories, LLD round, HLD probes |

**Recommended reading order:** `STORY.md` → `WORKFLOW.md` → drill into any
`services/*.md` → `NOTES.md` as a reference.

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

For the full bug-hunt / lessons section, see [War Stories in `STORY.md`](./docs/STORY.md#part-3--war-stories-the-bugs-and-what-they-taught-us).

---

## Stop / Reset

```powershell
# Stop everything, keep data
docker compose -f deployment\docker\docker-compose.yml down
cd "C:\Binary Labyrinth\VSCode Workspace\React Workspace\e-commerce-event-driven-shopping-app-frontend"
docker compose down

# Nuclear option: wipe volumes too (re-seeds catalog + admin on next run)
.\run.ps1 -Clean
```

---

## License & credits

Personal learning project. Built by **Binary Labyrinth** as a deep-dive into
production-style microservices patterns. See `docs/STORY.md` for the engineering
narrative.
