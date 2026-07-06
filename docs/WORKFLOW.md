# ShopSphere — System Workflow Overview

A Spring Boot **microservices** e-commerce backend with a React + TypeScript frontend.
Services discover each other through **Eureka**, talk synchronously via **Feign**
(load-balanced through Eureka) and asynchronously via **Kafka** events. All external
traffic enters through the **API Gateway** (single origin, no CORS for the SPA).

> Per-service detail lives in [`docs/services/`](./services). This file is the map.

---

## Service catalogue

| Service | Port | Datastore | Role |
|---|---|---|---|
| discovery-server | 8761 | – | Eureka service registry |
| config-server | 8888 | – | Centralised configuration |
| api-gateway | 8080 | – | Single entry point, routes `/api/**` |
| user-service | 8085 | MySQL `user_service` | Auth, JWT issuance, accounts |
| product-service | 8081 | MongoDB `product_service` | Catalog + full-text search |
| inventory-service | 8082 | MySQL `inventory_service` | Stock levels & reservations |
| order-service | 8083 | MySQL `order_service` | Order placement & lifecycle |
| payment-service | 8086 | MySQL `payment_service` | Stripe charges & refunds |
| cart-service | 8087 | MySQL `cart_service` | Per-user shopping cart |
| review-service | 8088 | MySQL `review_service` | Product reviews & ratings |
| recommendation-service | 8089 | MySQL `recommendation_service` | Co-purchase / trending recs |
| notification-service | 8084 | MySQL `notification_service` | Emails via MailHog (SMTP 1025) |
| admin-service | 8090 | – (aggregator) | Admin dashboard, Feign fan-out |

Infrastructure: **MySQL** (3307→3306 on host), **MongoDB** (27018→27017 on host),
**Kafka + Zookeeper**, **MailHog** (UI 8025). Both databases use persistent named
volumes (`mysql-data`, `mongo-data`).

---

## Security model

- **user-service** signs a stateless **JWT** at login (`sub`=email, claims `userId`,
  `role`). All services share the same HMAC secret (`JWT_SECRET`) so any service can
  validate a token without calling back.
- The **gateway permits `/api/**` through** — each downstream service runs its own
  `JwtAuthenticationFilter` and enforces roles (`ROLE_CUSTOMER` / `ROLE_ADMIN`).
- Public reads: product catalog & review listings. Everything else requires auth.
- **Admin promotion** is deliberate: registration defaults to `CUSTOMER`; an
  `ADMIN_REGISTRATION_KEY` (or the seeded `admin@shopsphere.com`) is required for ADMIN.
- When a service makes a Feign call that needs auth (e.g. order→user), it **forwards
  the caller's `Authorization` header** (see `FeignClientConfig`).

---

## The two flagship flows

### 1. Checkout (place order + pay)

```
Browser ──POST /api/orders──▶ Gateway ──▶ order-service
                                              │ 1. Feign POST /api/inventory/reserve  (reserve stock, forwards JWT)
                                              │ 2. Feign GET  /api/products/{id}       (authoritative unit price)
                                              │ 3. recompute price = unitPrice * qty   (client price IGNORED)
                                              │ 4. save Order(status=PLACED)
                                              │ 5. Kafka ▶ "order-placed"
                                              ▼
        ┌──────────────── Kafka "order-placed" ────────────────┐
        ▼                                                       ▼
 inventory-service                                       notification-service
 (emits "inventory-reserved")                            (sends order confirmation email)

Browser ──POST /api/payments──▶ Gateway ──▶ payment-service
                                              │ Stripe Charge (Idempotency-Key = userId-orderId)
                                              │ save Payment(COMPLETED|FAILED)
                                              │ Kafka ▶ "payment-processed" / "payment-failed"
                                              ▼
                                       notification-service (payment email)
```

Orders are intentionally **decoupled from payment** — an order persists even if the
card is declined, so it can be paid later.

> ⚠️ Backend models **one product per order**, so the frontend places **one order per
> cart line** then a single payment for the cart total.

### 2. Registration → welcome email

```
Browser ──POST /api/users/register──▶ user-service
                                         │ role = CUSTOMER, or ADMIN if adminKey matches
                                         │ BCrypt-hash password, save User
                                         │ Kafka ▶ "user-registered"
                                         ▼
                                  notification-service ──▶ MailHog (welcome email)
```

---

## Kafka topics (event bus)

| Topic | Producer | Consumers |
|---|---|---|
| `user-registered` | user-service | notification-service |
| `product-created` | product-service | inventory-service (auto-seeds stock row) |
| `item-added-to-cart`, `cart-cleared` | cart-service | (analytics / future) |
| `order-placed` | order-service | inventory-service, notification-service |
| `order-cancelled` | order-service | inventory-service (releases stock) |
| `inventory-reserved`, `inventory-failed` | inventory-service | order-service |
| `payment-processed`, `payment-failed`, `payment-refunded` | payment-service | notification-service |
| `review-submitted` | review-service | (recommendation / future) |

---

## Running it

See [`run.ps1`](../run.ps1) and the deployment README. Quick version:

```powershell
.\run.ps1            # Maven build -> backend -> frontend -> verify
.\run.ps1 -Clean     # wipe volumes and start from scratch
```

Build note: the service Dockerfiles `COPY target/*.jar` — they do **not** compile
inside Docker. Always run the Maven build (`mvnw clean package`) before
`docker compose ... up --build`. `run.ps1` does this for you.
