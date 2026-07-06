# ShopSphere — The Story of an E-Commerce Platform

*An in-depth narrative: the business case, the technical journey, the bugs we fought,
and a from-scratch blueprint to build it again.*

---

## Part 1 — The Business Story

### The problem

Imagine a mid-size retailer, "ShopSphere", that started life as a single PHP monolith.
For a while it worked. Then growth happened:

- **Black Friday took the whole site down.** A spike in product browsing exhausted the
  same database connections that checkout needed. Browsing traffic killed buying
  traffic — the most expensive failure mode in retail.
- **Releases were terrifying.** A one-line change to the review feature meant
  redeploying the entire application, so the team shipped once a month and prayed.
- **Teams stepped on each other.** The payments engineer and the search engineer were
  editing the same codebase, the same build, the same deploy.
- **The catalog and the ledger fought over the database.** Product data wants a
  flexible, document-shaped schema. Orders and payments want strict, transactional
  rows. Forcing both into one relational database made both worse.

The business didn't ask for "microservices." It asked for four outcomes:

1. **Browsing must never be able to take down buying.**
2. **Ship features independently and often, without full-site redeploys.**
3. **Survive partial failures** — if recommendations are down, the store still sells.
4. **Never lose an order or double-charge a customer.**

### The outcomes

ShopSphere was rebuilt as **13 small services**, each owning one capability and one
datastore. The business outcomes that fell out of the architecture:

| Business goal | How the architecture delivers it |
|---|---|
| Browsing can't kill checkout | Catalog (MongoDB) and orders/payments (MySQL) are physically separate services and databases. A browsing spike can't exhaust the order database. |
| Independent, frequent releases | Each service builds, tests, and deploys on its own. The reviews team never touches the payments deploy. |
| Survive partial failure | A **circuit breaker** around inventory means a slow inventory service degrades gracefully instead of cascading. Recommendations failing just hides a "you may also like" widget. |
| Correctness of money & stock | Orders are decoupled from payment (an order survives a declined card and can be paid later). Stripe charges use an **idempotency key** so a retry never double-charges. Stock is reserved transactionally with optimistic locking. |
| Trust & integrity | The **server recomputes order prices from the catalog** — a malicious client cannot forge a cheap price. Roles (CUSTOMER/ADMIN) are enforced on every service. |

The customer-facing result is an ordinary-looking storefront: browse, search, add to
cart, check out, pay, review, get recommendations, receive emails. The *engineering*
result is that each of those is a small, independently shippable, independently
scalable unit.

---

## Part 2 — The Technical Story

### The shape of the system

```
                         ┌─────────────────────────┐
   Browser / SPA  ─────▶ │   API Gateway (8080)    │  single origin, no CORS
                         └────────────┬────────────┘
                                      │  routes /api/** (lb:// via Eureka)
   ┌──────────────────────────────────┼──────────────────────────────────┐
   ▼            ▼           ▼          ▼          ▼          ▼             ▼
 user        product     inventory   order     payment     cart    review / rec / admin
 (MySQL)     (Mongo)     (MySQL)    (MySQL)    (MySQL)    (MySQL)        (MySQL)
   │            │           │          │          │
   └─── JWT ────┘           └── Kafka events (async) ──┘── notification-service ──▶ MailHog
        all services validate the same HMAC-signed token
```

- **Synchronous** calls (need an answer now): **Feign** clients, load-balanced through
  **Eureka**. Order→Inventory ("reserve this stock"), Order→Product ("what's the
  price"), Cart→Product ("price snapshot").
- **Asynchronous** calls (fire and forget): **Kafka** topics. Order placed → inventory
  reserves + notification emails. Payment processed → receipt email. Decoupling means
  the email service can be down without blocking checkout.
- **Polyglot persistence:** MongoDB for the flexible catalog and full-text search;
  MySQL for everything transactional.

### The key technical decisions and their outcomes

**1. Stateless JWT, validated everywhere.**
user-service is the only token *issuer*. It signs a JWT (`sub`=email, `userId`, `role`)
with a shared HMAC secret. Every other service validates locally — no chatty
"is this token valid?" callbacks. Outcome: services authorize independently and the
system stays horizontally scalable.

**2. Gateway routes, services authorize.**
The gateway lets `/api/**` through (`permitAll`) and forwards the `Authorization`
header. Each service runs its own `JwtAuthenticationFilter` and enforces roles. Outcome:
no single security chokepoint; each service owns its own access rules.

**3. Orders decoupled from payments.**
Placing an order and paying for it are two separate calls. An order persists as
`PLACED` even if Stripe declines. Outcome: no lost orders; "pay later" is possible;
payment retries are safe.

**4. Reserve-then-confirm with events.**
Order placement *synchronously* reserves stock (so two customers can't buy the last
unit), then *asynchronously* the inventory service emits `inventory-reserved`, which
flips the order `PLACED → CONFIRMED`. Outcome: strong consistency where it matters
(stock), eventual consistency where it's fine (status, emails).

**5. Server-authoritative pricing.**
The order total is computed server-side as `catalogUnitPrice × quantity`; the
client-supplied price is ignored. Outcome: closes a real money-integrity hole.

**6. Polyglot, persistent storage.**
MongoDB document model fits products; MySQL transactions fit orders/payments. Both sit
on **named Docker volumes** so data survives restarts.

---

## Part 3 — War Stories (the bugs, and what they taught us)

These actually happened while bringing the system up. They're the most instructive part.

### War story 1 — "Payments work… but every order is declined"
**Symptom:** valid Stripe test key, yet every charge failed.
**Cause:** the idempotency key was sent as a **request-body parameter**; modern Stripe
requires it as the **`Idempotency-Key` HTTP header**.
**Fix:** pass it via `RequestOptions`. **Lesson:** integration "works on my machine"
failures are often about *where* a value goes, not *whether* it's correct.

### War story 2 — "Orders return 403… no wait, the orders table is empty"
**Symptom:** placing an order returned a confusing 403; the UI sometimes looked like it
succeeded.
**Cause (layers, peeled one by one):**
1. The error *looked* like auth (403) but the request actually reached the controller.
2. The real failure was the inventory **circuit breaker fallback** firing.
3. The breaker fired because the inventory **reserve** call failed.
4. The reserve failed with a *business* 404: `InventoryNotFoundException`.
5. There was **no inventory row** for the product — because…
6. …MySQL had **no persistence volume**, so an earlier container recreate had silently
   wiped all inventory rows.
**Fix:** add a MySQL volume, re-seed inventory from the catalog, and make the circuit
breaker **ignore business exceptions** so "out of stock" doesn't trip it.
**Lesson:** a misleading status code (403) sent us hunting in the wrong layer. Always
follow the request to where the work actually happens (the logs at the business layer),
and distrust the surface symptom.

### War story 3 — "The data is in Docker but Compass shows nothing"
**Symptom:** `docker exec` showed 12 products in MongoDB; MongoDB Compass showed an
empty collection with the same name.
**Cause:** a **local MongoDB** on the host was also bound to `27017`, so Compass
connected to the *wrong* Mongo. The app's Mongo (in Docker) was a different instance.
**Fix:** remap Docker Mongo to host port `27018` (mirroring the MySQL `3307` pattern)
and add a volume. **Lesson:** port conflicts between host-native services and Docker
are invisible until they bite; remap deliberately.

### War story 4 — "I changed the code, rebuilt the image, nothing changed"
**Cause:** the service `Dockerfile`s `COPY target/*.jar` — they **don't compile inside
Docker**. Without running Maven first, `docker compose build` just repackages the *old*
jar.
**Fix:** always `mvnw clean package` before `docker compose ... up --build`; codified in
`run.ps1`. **Lesson:** know exactly where your build boundary is.

### War story 5 — "The circuit breaker protected us into an outage"
**Cause:** `ProductOutOfStockException` counted as a circuit-breaker failure. A handful
of out-of-stock attempts opened the breaker, which then rejected **every** order —
even for in-stock items.
**Fix:** `ignore-exceptions` for business exceptions. **Lesson:** circuit breakers must
distinguish "the dependency is sick" from "the answer is legitimately no."

> The throughline of every war story: **the symptom is rarely the cause.** Microservices
> turn a stack trace into a relay race across processes; debugging is about following the
> baton.

---

## Part 4 — Build It From Scratch

A pragmatic blueprint to recreate ShopSphere. This is the order we'd actually do it in.

### Phase 0 — Foundations (½ day)
1. **Pick the stack.** Spring Boot (Java), Spring Cloud (Gateway, Eureka, Config,
   OpenFeign), Spring Kafka, Spring Data (JPA + Mongo), Spring Security, Resilience4j.
   React + TypeScript + Vite for the SPA.
2. **Create a multi-module Maven project.** A parent `pom.xml`, a `common-library`
   module for shared Kafka event DTOs (`OrderPlacedEvent`, `ProductCreatedEvent`, …),
   and one module per service.
3. **Stand up infrastructure in `docker-compose.yml`:** MySQL, MongoDB, Kafka +
   Zookeeper, MailHog. Give MySQL and MongoDB **named volumes from day one** (learn from
   War story 2). Remap host ports that clash with local installs (MySQL `3307`, Mongo
   `27018`).

### Phase 1 — The backbone (1 day)
4. **discovery-server** (Eureka, `@EnableEurekaServer`, port 8761).
5. **config-server** (`@EnableConfigServer`, 8888) — optional but central.
6. **api-gateway** (Spring Cloud Gateway, 8080): one route per service
   (`/api/users/** → lb://user-service`, …), reactive `SecurityConfig` that permits
   `/api/**` and forwards `Authorization`.

### Phase 2 — Identity first (1 day)
7. **user-service** (8085, MySQL). Entity `User`, BCrypt passwords, `JwtUtil` (HMAC
   sign), register/login endpoints, a `JwtAuthenticationFilter` + `SecurityConfig`
   pattern you'll copy into every other service. Add a `DataSeeder` for a default admin
   and an `ADMIN_REGISTRATION_KEY` gate so admin signup is deliberate.
   *Why first:* everything else needs tokens to test against.

### Phase 3 — Catalog & stock (1–2 days)
8. **product-service** (8081, MongoDB). `Product` document with text indexes,
   CRUD + `/search`, public reads / ADMIN writes. Publish `product-created` on create.
   Add a `DataSeeder` of demo products so the storefront is never empty.
9. **inventory-service** (8082, MySQL). `Inventory(productId, quantity, @Version)`.
   Endpoints: availability check, **`/reserve`** (decrement, optimistic-locked), admin
   add/replenish. A `ProductCreatedConsumer` that seeds an inventory row from the
   product's `stock` whenever a product is created — so new products are orderable
   immediately.

### Phase 4 — The money path (2 days)
10. **cart-service** (8087, MySQL). One cart per user (keyed by JWT subject), lazy
    creation, quantity merge, **price snapshot** via Feign to product-service.
11. **order-service** (8083, MySQL). `placeOrder`: Feign-reserve stock → Feign-fetch
    authoritative price → recompute total → save `PLACED` → publish `order-placed`.
    Wrap in a **Resilience4j circuit breaker** with `ignore-exceptions` for business
    errors. Add an `InventoryEventConsumer` to advance `PLACED → CONFIRMED/CANCELLED`.
12. **payment-service** (8086, MySQL, Stripe). `processPayment` builds a Stripe Charge
    with the **idempotency key in the header**; persist COMPLETED/FAILED; publish
    `payment-processed`/`payment-failed`. Keep the Stripe key in a gitignored `.env`.

### Phase 5 — Engagement & ops (1 day)
13. **review-service** (8088): one review per (user, product) via a unique constraint;
    public listings, owner-only edits.
14. **recommendation-service** (8089): SQL co-purchase / trending queries over an
    interactions table.
15. **notification-service** (8084): Kafka consumers for `user-registered`,
    `order-placed`, `payment-*`; send to MailHog; persist an audit row per email.
16. **admin-service** (8090): a pure Feign aggregator behind `@PreAuthorize("hasRole
    ('ADMIN')")` powering the dashboard (stats, users, orders, low-stock, notifications).

### Phase 6 — The storefront (2–3 days)
17. **React + TS SPA**: AuthContext (JWT in localStorage, axios interceptors),
    CartContext, ToastContext. Pages for products/search, product detail
    (reviews + recs), cart, checkout (one order per line + one payment), my orders,
    my reviews, and an admin dashboard with a product-creation form.
18. **Ship it single-origin:** multi-stage Docker (Node build → nginx) where nginx
    proxies `/api/` to the gateway. No CORS in prod; Vite proxy does the same in dev.

### Phase 7 — Make it runnable & durable (½ day)
19. A **`run.ps1`** (or shell equivalent) that does the *correct* sequence:
    `mvnw clean package` → `docker compose up -d --build` (backend) → frontend →
    verify. Bake the Maven-first step in so it can't be forgotten (War story 4).
20. **Smoke test the golden path** end-to-end: register → browse → cart → checkout →
    pay → see order CONFIRMED → see email in MailHog → review → admin dashboard totals.

### Estimated effort
A focused engineer: ~**2 weeks** for a working vertical slice; a small team can
parallelize by service after Phase 2 (identity) is in place.

---

## Part 5 — What "good" looks like (acceptance checklist)

- [ ] Browsing load cannot exhaust the order/payment database (separate datastores).
- [ ] Any single non-critical service can be down and the store still sells.
- [ ] A declined payment leaves a recoverable, unpaid order — never a lost order.
- [ ] A retried payment never double-charges (idempotency key).
- [ ] A forged client price cannot change what the customer is billed.
- [ ] Two customers cannot both buy the last unit (transactional reserve + `@Version`).
- [ ] Out-of-stock is a normal "no", not a system outage (circuit breaker ignores it).
- [ ] Data survives `docker compose down && up` (named volumes).
- [ ] One command (`run.ps1`) builds and runs the whole stack correctly.

---

## Appendix — Where to read more

- System map & topic catalogue: [`WORKFLOW.md`](./WORKFLOW.md)
- Per-service deep dives: [`services/`](./services)
- Run script & build note: [`../run.ps1`](../run.ps1)
