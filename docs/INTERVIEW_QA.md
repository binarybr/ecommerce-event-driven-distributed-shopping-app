# ShopSphere — Interview Q&A

Backend-focused interview prep grounded in this project. Answers are crisp talking
points — expand with specifics from the code. Companion: [`NOTES.md`](./NOTES.md),
[`STORY.md`](./STORY.md), [`WORKFLOW.md`](./WORKFLOW.md).

---

## 1. Project & System Design

**Q: Walk me through your project.**
A 13-service e-commerce backend in Spring Boot with a React/TypeScript SPA. Each service
owns one capability and its own database. Services discover each other via Eureka, call
synchronously via Feign and asynchronously via Kafka, all behind a Spring Cloud Gateway.
Polyglot storage: MongoDB for the product catalog/search, MySQL for transactional
order/payment/inventory/user data. The headline flow is checkout: place order → reserve
stock → recompute price server-side → persist → emit events → pay via Stripe.

**Q: Why microservices and not a monolith?**
Three drivers: (1) **failure isolation** — browsing load (catalog) can't exhaust the
order/payment database because they're physically separate; (2) **independent deploys** —
ship reviews without redeploying payments; (3) **polyglot persistence** — products want a
document model, orders want transactions. I'd add: don't start with microservices for a
small app — the operational cost (distributed debugging, eventual consistency) is real.

**Q: How do services communicate?**
Synchronous (need an answer now) → **Feign** over Eureka load-balancing, e.g. order→inventory
"reserve this stock." Asynchronous (fire-and-forget) → **Kafka**, e.g. `order-placed` triggers
inventory and email reactions without blocking checkout.

**Q: How do you decide sync vs async?**
If the caller can't proceed without the result and it must be consistent *now* (stock
reservation, price), it's synchronous. If it's a side-effect that can happen slightly later
and shouldn't block the user (emails, status updates, analytics), it's an event.

**Q: How would you scale this?**
Stateless services scale horizontally behind Eureka + the gateway. Hot paths (catalog reads)
scale independently of cold paths (checkout). Add read replicas for MySQL, shard MongoDB,
partition Kafka topics, and cache catalog reads. JWT being stateless means no shared session
store is needed.

---

## 2. Spring / Spring Cloud

**Q: What does the API Gateway do, and does it handle auth?**
It's the single entry point; routes `/api/**` by path to services (`lb://…`). It does **not**
authenticate — it permits `/api/**` through and forwards the `Authorization` header. Each
service validates the JWT itself. This avoids a single security chokepoint and lets each
service own its access rules.

**Q: What is Eureka and why is it needed?**
A service registry. Services register on startup; Feign and the gateway resolve names
(`order-service`) to instances instead of hard-coded IPs — enabling relocation and load
balancing. A `503` at the gateway typically means the target hasn't registered yet.

**Q: How does Feign work here?**
Declare an interface with Spring MVC annotations; Feign generates the HTTP client and
load-balances via Eureka. Example: `@FeignClient(name="product-service")` with
`@GetMapping("/api/products/{id}")`. For authenticated downstream calls, a
`RequestInterceptor` forwards the caller's JWT.

**Q: Gateway is reactive but services are MVC — why?**
Spring Cloud Gateway is built on WebFlux (non-blocking, good for a high-throughput proxy).
The business services use blocking MVC + JPA, which is simpler for transactional CRUD. Right
tool per layer.

---

## 3. Data & Persistence

**Q: Why MongoDB for products and MySQL for orders?**
Products have variable attributes and need full-text search → document model + text indexes.
Orders/payments need ACID transactions, foreign-key-like integrity, and money correctness →
relational. Forcing both into one store makes both worse.

**Q: How do you prevent two customers buying the last item?**
Stock reservation is a transactional decrement guarded by **JPA optimistic locking**
(`@Version`). If two requests race, one save fails the version check and is retried/rejected,
so stock never goes negative.

**Q: Catalog says 5 in stock but order fails — why?**
The product's catalog `stock` is metadata; **orderable availability lives in
inventory-service**. A product needs an inventory row (auto-seeded from a `product-created`
Kafka event). This separation is deliberate — catalog and warehouse are different concerns.

---

## 4. Security

**Q: Explain your auth model.**
Stateless JWT. user-service signs a token (`sub`=email, `userId`, `role`) with a shared HMAC
secret on login. Every service validates locally — no auth callbacks. The gateway forwards
the token; each service's `JwtAuthenticationFilter` sets the security context and enforces
roles (CUSTOMER/ADMIN).

**Q: Why JWT over server sessions?**
Stateless → no shared session store, services scale independently, and any service can
authorize from the token alone. Trade-off: tokens can't be trivially revoked before expiry
(mitigate with short expiry / a denylist if needed).

**Q: How is admin access controlled?**
Registration defaults to CUSTOMER. ADMIN requires either the seeded admin account or a valid
`ADMIN_REGISTRATION_KEY` — no self-service privilege escalation. Admin endpoints are gated by
`hasRole('ADMIN')` (URL- and method-level).

**Q: How are passwords stored?**
BCrypt (salted, slow, one-way). `encode` on register; `matches` (constant-time) on login.
Plaintext is never stored or logged.

---

## 5. Messaging & Resilience

**Q: What's Kafka used for here?**
The event backbone: `order-placed`, `payment-processed`, `user-registered`, `product-created`,
etc. Decouples producers from consumers — e.g. notification-service sends emails reacting to
events without checkout depending on it.

**Q: What's a circuit breaker and where do you use one?**
Resilience4j around the order→inventory call. After 50% failures in a 10-call window it
"opens" and short-circuits to a fallback for 10s, then half-opens to test recovery. Prevents
a slow/dead inventory service from cascading.

**Q: (Follow-up) What was the bug with it?**
I had `ProductOutOfStockException` counting as a breaker failure, so a few out-of-stock
attempts opened the breaker and rejected **all** orders. Fix: `ignore-exceptions` for business
exceptions — the breaker should only react to *infrastructure* failures, not legitimate "no."

---

## 6. Payments & Correctness (high-signal)

**Q: How do you prevent double-charging on a retry?**
Stripe **idempotency key** = `userId-orderId`, sent as the `Idempotency-Key` **header** (via
`RequestOptions`). Stripe dedupes — a retried charge for the same order is a no-op. (I hit a
bug putting it in the body, which Stripe rejects.)

**Q: What happens if payment fails after the order is placed?**
Nothing is lost — orders are **decoupled** from payments. The order stays `PLACED` and is
payable later; the failed attempt is persisted as an audit row (`@Transactional(noRollbackFor
= PaymentException.class)` keeps that row despite the rethrow).

**Q: A client sends price=0.01 for a $350 item — what happens?**
Rejected at the value level: order-service **ignores the client price** and recomputes
`unitPrice × quantity` from the catalog via Feign. This closed a real billing-integrity hole.

---

## 7. Debugging / War Stories (behavioral + depth)

**Q: Tell me about a hard bug you debugged.**
Orders silently failed. The symptom was a misleading **403**, but the request actually reached
the controller — the real cause was the inventory **circuit-breaker fallback**, which fired
because the **reserve** call returned a business **404** (`InventoryNotFoundException`),
because there were **no inventory rows**, because **MySQL had no persistence volume** and a
container recreate had wiped them. Fix: add named volumes, re-seed inventory, and make the
breaker ignore business exceptions. **Lesson: the symptom is rarely the cause — follow the
request to the business layer's logs.**

**Q: Data was in Docker but the GUI showed nothing — what was it?**
A local MongoDB on the host was bound to 27017, so Compass connected to the wrong instance.
Fix: remap Docker Mongo to host port 27018 (like MySQL's 3307) and add a volume.

**Q: "I changed code, rebuilt, nothing changed" — why?**
The Dockerfiles `COPY target/*.jar` — they don't compile inside Docker. Without a Maven build
first, the image repackages the stale jar. Fixed by codifying `mvnw clean package` before
`docker compose build` in a run script.

---

## 8. Java / Language

**Q: Which Java features did you use?**
Streams, `Optional`, lambdas, `java.time` (Java 8); `Optional.ifPresentOrElse` and `List.of`
(9); `var` (10); `Stream.toList()` (16). Lombok fills the boilerplate role.

**Q: What would you modernize?**
Java 25 is the runtime, but the style is Java 8/9-era. I'd adopt **records** for immutable
DTOs, **pattern-matching `instanceof`/`switch`**, and **text blocks** for SQL/JSON literals.

**Q: `Optional` — when not to use it?**
Great for return types signaling "maybe absent." Avoid as a field or method parameter, and
don't call `.get()` without checking — use `orElseThrow`/`map`/`ifPresentOrElse`.

---

## 9. Rapid-fire / Gotchas

- **Idempotent vs safe?** GET is safe (no change). PUT/reserve are idempotent-ish; use keys
  for true idempotency on POST.
- **`@Transactional` on a private method?** Won't work — Spring AOP proxies public methods.
- **Why `noRollbackFor` in payments?** To persist a FAILED audit row even when we rethrow.
- **Eventual consistency example?** Order `PLACED` → Kafka `inventory-reserved` →
  `CONFIRMED`.
- **Consumer group purpose?** One member of a group processes each message (scales consumers).
- **Why server-side price?** Never trust client input for money.
- **Gateway returns 503 vs 403?** 503 = no service instance (Eureka); 403 = service rejected
  the role.

---

## 10. Behavioral framing

**Q: What are you most proud of?**
Owning correctness end-to-end — money (idempotency, server-side pricing), stock (transactional
reserve), and resilience (circuit breaker tuned for business vs infra failures) — and
systematically root-causing distributed bugs rather than patching symptoms.

**Q: What would you do differently?**
Add persistence volumes and contract tests from day one, introduce a saga/outbox pattern to
remove the reserve-before-save leak, and add distributed tracing (e.g. OpenTelemetry) so
cross-service debugging doesn't rely on per-service log spelunking.

---

## 11. Low-Level Design Round — "Whiteboard the checkout flow"

A common ask: walk through the design of the order + payment + inventory interaction.
Below is a ready-to-narrate answer.

### Sequence diagram (ASCII)

```
Client    Gateway   order-svc   inventory-svc   product-svc   Kafka         payment-svc   notif-svc
  │          │          │             │              │           │              │            │
  │ POST /api/orders               │             │              │           │              │            │
  ├─────────▶│          │             │              │           │              │            │
  │          ├─────────▶│             │              │           │              │            │
  │          │          │ POST /reserve (Feign, JWT) │           │              │            │
  │          │          ├────────────▶│              │           │              │            │
  │          │          │  inStock=true (decrement)  │           │              │            │
  │          │          │◀────────────┤              │           │              │            │
  │          │          │ GET /products/{id} (price) │           │              │            │
  │          │          ├──────────────────────────▶ │           │              │            │
  │          │          │◀──────────────────────────┤            │              │            │
  │          │          │ save Order(PLACED, price=unit*qty)     │              │            │
  │          │          │ publish "order-placed"  ─────────────▶│               │            │
  │          │          │             │              │           │              │            │
  │          │ 201 Created (order)    │              │           │              │            │
  │◀─────────┤◀─────────┤             │              │           ├───emit──────▶│ "order-placed" → email
  │                                                              │              │            │
  │                                                              │  ─────────▶ "inventory-reserved"
  │          │          │ consume "inventory-reserved" → PLACED→CONFIRMED       │            │
  │                                                                                          │
  │ POST /api/payments                                                                       │
  ├─────────▶│ ─────────────────────────────────────────────────────────────────▶ Stripe Charge
  │          │                                                                  │ (Idempotency-Key header)
  │          │                                                                  │ save Payment(COMPLETED|FAILED)
  │          │                                                                  ├─── publish "payment-processed"
  │          │                                                                  │            │── email
  │◀─────────┤◀──────────────────────────────────────────────────────────────────┤            │
```

### DB schema (the parts that matter)

```sql
-- order_service.orders (MySQL)
id BIGINT PK AUTO_INCREMENT,
order_number VARCHAR(36) UNIQUE,    -- UUID; cross-service correlation id
user_id      VARCHAR,                -- JWT subject
product_id   VARCHAR,                -- MongoDB ObjectId
quantity     INT,
price        DECIMAL(10,2),          -- server-computed = unit_price * qty
status       VARCHAR,                -- PLACED | CONFIRMED | CANCELLED | DELIVERED
created_at   DATETIME

-- inventory_service.inventory (MySQL)
id          BIGINT PK,
product_id  VARCHAR UNIQUE,
quantity    INT,
version     INT                      -- @Version for optimistic locking

-- payment_service.payment (MySQL)
id              BIGINT PK,
user_id         VARCHAR,
order_id        VARCHAR,             -- = orders.order_number
amount          DECIMAL(10,2),
status          VARCHAR,             -- COMPLETED | FAILED | REFUNDED
transaction_id  VARCHAR,             -- Stripe charge id (ch_...)
error_message   VARCHAR,
created_at      DATETIME,
completed_at    DATETIME
```

### Talking points to weave in

- **`order_number` is a UUID** so every service can reference an order without
  coordinating on a DB auto-increment — exactly what you need for distributed
  correlation.
- **Reservation is synchronous, status update is asynchronous.** Strong consistency
  where stock matters; eventual consistency where it's fine.
- **`@Version` on inventory** turns concurrent reserves into safe retries — no
  oversells.
- **Authoritative price in `orders.price`** is the *server-computed* total — the API
  rejects the client's value to prevent forged-price attacks.
- **`payment.order_id` links to `orders.order_number`** (not the auto-increment id) so
  the payment record survives even if an order row is re-keyed or replayed.
- **Idempotency** at two levels: Stripe's `Idempotency-Key` header
  (`userId-orderId`) for charges, and `order_number` uniqueness for orders.

### Drawing tip
Draw three boxes (services), arrows for Feign calls (solid) and Kafka events (dashed),
mark which steps are inside a DB transaction. The interviewer wants to see you label
*sync vs async*, *which side owns the truth*, and *where you've put correctness
guarantees* (idempotency, locking, server-side computation).

---

## 12. HLD / LLD Probes — common follow-ups & how to handle them

Sharp, time-boxed answers to the "what if…" questions interviewers spring late in the
round. Each one is a hook — be ready to go one level deeper if pushed.

### "How would you handle 10× traffic?"
- **Read scale:** the catalog is the hottest path → add a **Redis cache** in front of
  product reads with a short TTL; serve search from a read-optimised store (Mongo
  replicas or move search to **OpenSearch/Elasticsearch**).
- **Write scale:** Kafka topics are already partitioned by `order_number` /
  `product_id` → scale consumer instances horizontally (one per partition).
- **DB scale:** MySQL read replicas for order/inventory reads; vertical-scale the
  primary for writes; **shard inventory by `product_id`** if it becomes a hot spot.
- **Service scale:** services are stateless behind Eureka — scale instances
  horizontally; Spring Cloud LoadBalancer already distributes.
- **Backpressure & queueing:** keep order placement synchronous, but offload
  notifications, recommendations, and analytics entirely to Kafka so the user-facing
  path stays fast.

### "Make ordering fully idempotent."
- Frontend generates a UUID `clientRequestId` per checkout attempt.
- Add a `client_request_id` unique column on `orders`. The first POST creates the row;
  a retry with the same id returns the existing order (200 OK, not a duplicate).
- For Stripe, the `Idempotency-Key` header is already keyed by `userId-orderId`.
- Net effect: a network blip + retry never creates two orders or two charges.

### "Eliminate the reservation leak you mentioned."
Use the **outbox pattern + a saga**:
1. In one DB transaction, write the Order **and** an `outbox(reserve_command)` row.
2. A relay polls the outbox and calls inventory-service.
3. Inventory emits `inventory-reserved` or `inventory-failed`.
4. order-service consumes that event and either confirms or compensates (cancels the
   order). Stock is only debited after the order is durable, so a save-failure can't
   leak units.

### "What if Stripe is down for 30 minutes?"
- POST /payments returns a clear retryable error; the order stays `PLACED`.
- A scheduled job retries pending payments with **exponential backoff + jitter**, all
  keyed by the existing idempotency key (safe to retry).
- Surface "pending payment" status in the UI; let the user retry manually too.
- Add a **Stripe webhook** consumer to reconcile async charges (mark COMPLETED on
  `charge.succeeded`).

### "How would you make this multi-tenant?"
- Add a `tenant_id` claim to the JWT (issued by user-service).
- A **Spring tenant filter** stuffs it into a `ThreadLocal`; repositories add an
  automatic `WHERE tenant_id = ?` (Hibernate filter or per-tenant schema).
- For Mongo, add `tenant_id` as an index prefix on every query.
- Kafka events carry `tenant_id`; consumers filter or partition by it.
- Operational choice: shared schema (cheap, weaker isolation) vs schema-per-tenant
  (stronger isolation, more ops).

### "How do you debug a slow checkout in production?"
- **Distributed tracing** (OpenTelemetry → Jaeger/Tempo): one trace id flows through
  gateway → order → inventory → product → kafka → payment; spot the slow span.
- **Metrics** (Micrometer → Prometheus → Grafana): p50/p95/p99 per endpoint, circuit
  breaker state, Kafka consumer lag, DB connection-pool saturation.
- **Logs** correlated by `traceId` + `order_number`.
- The dashboard tells you *which hop* is slow; the trace tells you *which call*; the
  log tells you *why*.

### "What if MongoDB goes down?"
- Catalog reads degrade gracefully — cache last-known products in Redis with a longer
  fallback TTL; show "search temporarily unavailable" if total outage.
- Catalog writes (admin) are rejected with a 503.
- The store still **sells** — `order-service` needs product *price* from Mongo, so add
  a price cache; or denormalize the unit price into the cart at add-time and snapshot
  it onto the order. (Cart already snapshots — we'd extend the pattern.)

### "Add a wishlist feature. How?"
- New `wishlist-service` (port 8091, MySQL): `wishlist(user_id, product_id, added_at)`.
- Endpoints: `GET/POST/DELETE /api/wishlist/items`; gateway route added.
- "Move to cart" simply calls cart-service via Feign with the saved product ids.
- No changes needed to existing services — that's the whole point of the architecture.

### "Two services need to talk — Feign or Kafka?"
- **Need the answer to proceed?** Feign (price lookup, stock reserve).
- **Side effect, can be retried, doesn't block the user?** Kafka (emails, indexing,
  analytics, status updates).
- **Cross-service transaction?** Neither directly — use a **saga** with compensating
  events.

### "What's wrong with synchronous Feign chains?"
Latency adds up; one slow service slows the whole chain; cascading failures. Mitigate
with circuit breakers + timeouts + bulkheads, and prefer **event-driven** flows for
non-blocking work. *In this project, order → inventory is the only sync hop on the
critical path — intentionally.*

### "How do you handle schema migration on a live service?"
- Use **Flyway/Liquibase** instead of `ddl-auto=update` (which is fine for dev,
  dangerous in prod).
- Expand-then-contract: add new columns → backfill → switch reads → drop old. Never
  rename in place under live traffic.
- For Mongo: add fields opportunistically; never delete in the same release that stops
  reading them.

### "What's the blast radius if one service is compromised?"
- Service runs as a non-root container with only its DB credentials.
- It can call other services only via the gateway → blocked by their JWT/RBAC checks.
- Stripe key only lives in payment-service (`.env`, gitignored).
- Lateral movement is limited to whatever its own JWT role can do.
- *Improvement:* mutual-TLS between services and a secrets manager (Vault / AWS
  Secrets Manager) instead of env files.
