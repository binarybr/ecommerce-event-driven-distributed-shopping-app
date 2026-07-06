# ShopSphere - Design Approach

## 1. Design Intent

ShopSphere is designed as a production-style e-commerce backend rather than a simple
CRUD application. The main goal is to show how a real purchase journey can be split
across independently deployable services while still preserving the properties that
matter most in commerce: secure identity, correct pricing, correct stock handling,
auditable payments, and graceful behavior when a dependency is unhealthy.

The design follows these principles:

- Keep each business capability behind its own service boundary.
- Let each service own its data instead of sharing tables across services.
- Use synchronous calls only when the caller needs an immediate decision.
- Use events for side effects and cross-service reactions that can happen after the
  primary request completes.
- Treat price, payment, and inventory as server-controlled concerns.
- Prefer local JWT validation over central auth lookups on every request.
- Containerize the stack so the system can be run and tested as a whole.

## 2. Architectural Style

The application uses a microservices architecture with thirteen services:

| Category | Services |
|---|---|
| Infrastructure | discovery-server, config-server, api-gateway |
| Core commerce | user-service, product-service, inventory-service, order-service, payment-service, cart-service |
| Engagement and operations | review-service, recommendation-service, notification-service, admin-service |

The architecture is intentionally not a distributed monolith. Services are separated
by business ownership:

- `user-service` owns accounts, registration, login, roles, and JWT issuance.
- `product-service` owns catalog data and search.
- `inventory-service` owns available stock and stock reservation.
- `order-service` owns order creation and order lifecycle.
- `payment-service` owns Stripe charge, refund, and payment audit records.
- `cart-service` owns each user's cart.
- `notification-service` owns email delivery from events.
- `admin-service` aggregates operational views through service APIs instead of
  reading other services' databases.

This decomposition keeps high-change areas independent. For example, catalog search
can evolve without changing order persistence, and payment handling can change without
requiring catalog schema changes.

## 3. Entry Point and Routing

All client traffic enters through `api-gateway` on port `8080`. The gateway uses
Spring Cloud Gateway routes such as:

- `/api/users/**` -> `user-service`
- `/api/products/**` -> `product-service`
- `/api/orders/**` -> `order-service`
- `/api/payments/**` -> `payment-service`
- `/api/admin/**` -> `admin-service`

Routes use `lb://service-name`, so the gateway discovers service instances through
Eureka instead of hard-coding host names. This gives the system a single external API
surface while allowing the internal services to scale or move independently.

The gateway is deliberately thin. It routes requests and provides the front door, but
authorization is enforced inside each downstream service. This avoids making the
gateway the only security boundary.

## 4. Service Discovery and Configuration

`discovery-server` provides a Eureka registry. Backend services register themselves
with Eureka, and both the gateway and Feign clients resolve services by name. This
removes direct coupling to container IP addresses and supports Docker Compose as well
as future clustered deployment.

`config-server` provides a central place for shared service configuration. The project
still keeps practical local defaults in `application.yaml` files, with environment
variables overriding values for Docker or cloud deployment. This approach keeps local
development simple while allowing deployment-specific configuration to be injected
without code changes.

## 5. Communication Strategy

The project uses two communication styles.

### Synchronous HTTP With Feign

Feign is used where the caller must know the result before it can continue:

- `order-service` calls `inventory-service` to reserve stock before accepting an order.
- `order-service` calls `product-service` to fetch the authoritative product price.
- `order-service` calls `user-service` to resolve customer email for notifications.
- `admin-service` calls multiple services to build dashboard views.

These calls are direct business dependencies. For example, an order cannot be accepted
if stock cannot be reserved or if the product price cannot be resolved.

Feign calls that need user context forward the inbound `Authorization` header through
`FeignClientConfig`. This keeps downstream authorization checks meaningful instead of
bypassing service security.

### Asynchronous Events With Kafka

Kafka is used where the system needs to notify other services without blocking the
main request:

| Event topic | Producer | Main consumers |
|---|---|---|
| `user-registered` | user-service | notification-service |
| `product-created` | product-service | inventory-service |
| `order-placed` | order-service | inventory-service, notification-service |
| `order-cancelled` | order-service | inventory-service |
| `inventory-reserved`, `inventory-failed` | inventory-service | order-service |
| `payment-processed`, `payment-failed`, `payment-refunded` | payment-service | notification-service |
| `review-submitted` | review-service | future recommendation/analytics consumers |

Events are represented in `common-library`, which keeps producer and consumer payloads
consistent. The design accepts eventual consistency for side effects such as email
delivery and status updates, but keeps checkout-critical checks synchronous.

## 6. Data Ownership and Persistence

Each service owns its persistence model. Other services interact through APIs or
events, not through direct database access.

| Service | Datastore | Reason |
|---|---|---|
| product-service | MongoDB | Product documents are flexible, category-specific, and searchable. |
| user-service | MySQL | Account records need transactional updates and uniqueness constraints. |
| inventory-service | MySQL | Stock changes require transactional consistency. |
| order-service | MySQL | Orders are transactional business records. |
| payment-service | MySQL | Payment attempts and refunds need durable audit history. |
| cart-service | MySQL | Cart state is relational and user-scoped. |
| review-service | MySQL | Reviews and rating summaries fit relational access patterns. |
| recommendation-service | MySQL | Current recommendation data is simple and transactional. |
| notification-service | MySQL | Notification records are auditable delivery attempts. |

The most deliberate persistence split is product data versus commerce transaction
data. MongoDB is used for the catalog because products can vary by category and benefit
from text search. MySQL is used for orders, payments, inventory, users, and reviews
because those workflows need transactions, constraints, and clear auditability.

## 7. Security Design

Authentication starts in `user-service`. On login, the service signs a JWT containing
the user's identity and role. Every protected service validates the JWT locally using
the shared secret.

This design avoids a central auth callback on every request. If `user-service` is
temporarily unavailable, already-issued tokens can still be validated by other services.

Authorization is role-based:

- Public endpoints allow catalog browsing and public review reads.
- Customer endpoints require a valid customer or admin JWT.
- Admin endpoints require `ROLE_ADMIN`.
- Admin registration is protected by a registration key, and normal registration
  defaults to customer access.

Each service has its own `SecurityConfig`, `JwtAuthenticationFilter`, and `JwtUtil`.
That duplication is intentional in this project because it makes each service runnable
and enforceable on its own. A future hardening step would be extracting repeated
security code into a shared internal starter.

## 8. Checkout and Order Design

Checkout is the most important workflow in the system. The design is server
authoritative:

1. The client submits product ID, quantity, and user/order intent.
2. `order-service` asks `inventory-service` to reserve stock.
3. `order-service` asks `product-service` for the current product price.
4. The order total is recomputed on the server as `unitPrice * quantity`.
5. The order is saved with status `PLACED`.
6. `order-service` publishes `order-placed`.
7. Payment is processed separately through `payment-service`.
8. Payment events drive notification side effects.

The important security choice is that client-supplied price is ignored. This prevents
a forged request from underpaying for an item.

Orders and payments are intentionally decoupled. An order can exist even when payment
fails, allowing the user to retry payment and allowing the system to keep an audit
record of the attempt.

## 9. Inventory and Concurrency Design

Inventory uses MySQL and optimistic locking through a JPA `@Version` field on the
`Inventory` entity. When two customers try to reserve the final units at the same time,
Hibernate can detect conflicting updates instead of silently overwriting stock.

The implemented reservation flow is:

- Read inventory row by `productId`.
- Check whether available quantity is sufficient.
- Decrement quantity inside a transaction if stock is available.
- Return an `inStock` decision to the order flow.

The design also includes compensation for cancellations: when an order is deleted or
cancelled, `order-service` emits `order-cancelled`, and `inventory-service` releases
the reserved quantity.

Known limitation: in the current implementation, stock reservation happens before the
order row is persisted. If reservation succeeds but order persistence fails, reserved
stock can leak unless compensated by a later repair process. A more robust production
design would use a saga with explicit reservation IDs, reservation expiry, and an
outbox pattern so every state transition and event publish is durable.

## 10. Payment Design

`payment-service` integrates with Stripe in test mode. The design choices are:

- Payment state is persisted locally in MySQL for auditability.
- Stripe amounts are sent in minor units.
- Successful payments publish `payment-processed`.
- Failed payments publish `payment-failed`.
- Refunds are allowed only for completed payments and publish `payment-refunded`.
- Stripe idempotency uses `RequestOptions.setIdempotencyKey(userId + "-" + orderId)`.

The idempotency key is important because network retries should not double-charge a
customer. Failed Stripe attempts are saved with `@Transactional(noRollbackFor =
PaymentException.class)`, so the audit row survives even when the service rethrows an
error to the caller.

## 11. Resilience Design

The order flow depends on inventory. To prevent cascading failures, `order-service`
wraps the inventory dependency with a Resilience4j circuit breaker.

The circuit breaker is tuned to ignore business exceptions such as out-of-stock and
order-not-found. Those are valid business outcomes, not signs that the dependency is
unhealthy. Only infrastructure-style failures should contribute to opening the
circuit.

This distinction matters: without it, a burst of out-of-stock orders would make the
system treat inventory as down and reject unrelated in-stock purchases.

## 12. Notification Design

Notifications are event-driven. Services do not send email directly during customer
flows. Instead, they publish business events and `notification-service` converts those
events into email messages.

This keeps customer-facing requests fast and prevents SMTP failures from breaking core
flows such as registration, order placement, or payment. In development, MailHog is
used as the SMTP target so messages can be inspected without sending real email.

## 13. Admin and Aggregation Design

`admin-service` acts as an aggregator for operational views. It does not own the source
data for users, products, orders, payments, reviews, or inventory. Instead, it calls the
owning services and assembles dashboard responses.

This preserves service ownership while giving administrators a single backend API for
cross-service screens. The tradeoff is that admin views depend on multiple downstream
services and can degrade if one dependency is unavailable.

## 14. Deployment Design

The backend is a Maven multi-module project. The root `pom.xml` builds:

- `common-library`
- infrastructure services
- business services
- deployment modules

Docker Compose runs the full local stack:

- MySQL
- MongoDB
- Kafka and Zookeeper
- MailHog
- Eureka, Config Server, Gateway
- all business services

The service Dockerfiles copy pre-built JARs from `target`, so the Maven build must run
before Docker image creation. `run.ps1` encodes this workflow and provides a repeatable
local startup path.

Persistent Docker volumes are used for MySQL and MongoDB so application data survives
container rebuilds and restarts.

## 15. API and DTO Design

Controllers expose REST APIs under `/api/...`. Services use DTOs for request and
response models instead of exposing entities directly. This keeps persistence details
separate from API contracts and lets services add internal fields without breaking
clients.

Validation is handled at the API boundary with Bean Validation annotations where
applicable. Global exception handlers translate domain and validation failures into
HTTP responses.

The shared event DTOs are intentionally centralized in `common-library` because Kafka
payloads are contracts between services. REST DTOs remain service-local because they
belong to each service's API boundary.

## 16. Observability and Operations

Services expose Spring Boot Actuator endpoints for health and metrics. This supports:

- Docker health checks.
- Eureka registration visibility.
- Basic runtime diagnostics.
- Future Prometheus/Grafana integration.

Logging is used around key business operations such as order creation, stock
reservation, payment success/failure, refunds, and event publishing.

## 17. Tradeoffs

| Decision | Benefit | Tradeoff |
|---|---|---|
| Microservices | Independent ownership and deployment | More moving parts and distributed failure modes |
| Per-service databases | Strong service autonomy | Cross-service queries require APIs or aggregation |
| Kafka for side effects | Loose coupling and non-blocking flows | Eventual consistency and retry concerns |
| Feign for checkout decisions | Immediate correctness for stock and price | Runtime dependency between services |
| Local JWT validation | No auth-service callback per request | Shared secret management across services |
| MongoDB for products | Flexible catalog and search | Different operational model than MySQL |
| Stripe idempotency | Retry-safe payments | Requires careful key design |
| Optimistic locking | Prevents lost stock updates | Callers must handle conflict/retry scenarios |

## 18. Future Design Improvements

The current implementation demonstrates the main patterns clearly. For a production
system, the next design improvements would be:

- Add an outbox pattern for reliable event publishing after database commits.
- Model inventory reservations explicitly with reservation IDs, expiry, and release.
- Add a saga/process manager for order, inventory, and payment state transitions.
- Add distributed tracing with OpenTelemetry.
- Move repeated JWT/security code into a shared internal Spring Boot starter.
- Add idempotency keys to order creation, not only payment creation.
- Add dead-letter topics and retry policies for Kafka consumers.
- Use managed cloud services for database, messaging, secrets, and load balancing.
- Add stronger automated integration tests around checkout, stock races, and payment
  retry behavior.

## 19. Summary

ShopSphere's design uses microservices where the service boundaries map to real
e-commerce capabilities. The system keeps critical decisions, such as price and stock,
on the server; uses synchronous calls for immediate checkout decisions; uses Kafka for
event-driven side effects; and protects money movement with persisted payment records
and Stripe idempotency.

The design is intentionally pragmatic: it is small enough to run locally with Docker
Compose, but it still demonstrates the architectural concerns that matter in a larger
commerce system: ownership, security, consistency, fault isolation, and operational
repeatability.
