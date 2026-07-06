# Project Synopsis

## Title
**ShopSphere — A Microservices-Based Online Shopping Platform**

---

## 1. Abstract

ShopSphere is a full-stack e-commerce application built on a **microservices
architecture** using Spring Boot and a React + TypeScript front end. It decomposes a
traditional online-store monolith into **thirteen independently deployable services**,
each owning a single business capability and its own datastore. Services discover one
another through a service registry, communicate synchronously via declarative HTTP
clients and asynchronously through an event-streaming platform, and are fronted by a
single API gateway.

The platform implements the complete retail journey — user registration and
authentication, product catalogue and full-text search, shopping cart, order placement,
inventory reservation, online payment, reviews, recommendations, email notifications,
and an administrative dashboard. The project demonstrates production-grade distributed-
systems concerns: stateless token-based security, role-based access control, fault
tolerance via circuit breakers, transactional correctness, idempotent payments, and
containerised deployment.

---

## 2. Introduction

Monolithic e-commerce systems suffer from poor fault isolation (a spike in browsing can
exhaust the resources needed for checkout), painful all-or-nothing releases, and a single
database forced to serve conflicting workloads. ShopSphere addresses these problems by
splitting the system into small services that can be developed, deployed, and scaled
independently, and by applying **polyglot persistence** — a document database for the
flexible, searchable product catalogue and a relational database for transactional order
and payment data.

---

## 3. Problem Statement

To design and implement a scalable, resilient, and secure online shopping platform that:
- isolates failures so non-critical components cannot bring down the purchase flow,
- supports independent development and deployment of features,
- guarantees correctness of money and stock under concurrency, and
- can be containerised and deployed to the cloud at low cost.

---

## 4. Objectives

1. Decompose an e-commerce system into cohesive, loosely-coupled microservices.
2. Implement secure, stateless authentication (JWT) with role-based authorization.
3. Enable both synchronous (request/response) and asynchronous (event-driven) inter-
   service communication.
4. Apply polyglot persistence (relational + document databases).
5. Ensure transactional correctness — no overselling of stock, no double-charging.
6. Build a responsive single-page front end consuming the services through a gateway.
7. Containerise the entire stack and provide a repeatable build-and-run pipeline.

---

## 5. Scope

**In scope:** customer and admin web flows; catalogue + search; cart; order lifecycle;
inventory reservation; Stripe-based test payments; reviews and ratings; basic
recommendations; transactional email (development SMTP); admin dashboard; containerised
local and single-host cloud deployment.

**Out of scope (future work):** real production payment go-live, multi-region high
availability, mobile apps, advanced ML-based recommendations, and managed-service cloud
deployment (ECS/EKS/RDS) — discussed under Future Scope.

---

## 6. System Architecture

```
            Browser / React SPA
                    │
            API Gateway (routing, single origin)
                    │  service discovery + load balancing
 ┌───────────┬──────┴───────┬────────────┬───────────┬─────────────┐
 User      Product       Inventory     Order       Payment      Cart / Review /
 Service   Service       Service       Service      Service      Recommendation / Admin
 (SQL)     (NoSQL)       (SQL)         (SQL)        (SQL)        (SQL)
   │           │             │            │            │
   └─── stateless JWT validated by every service ───────┘
                    │
              Event bus (Kafka) ──▶ Notification Service ──▶ Email (SMTP)
```

- **Synchronous calls** (need an immediate answer) use declarative HTTP clients
  (OpenFeign) load-balanced through the service registry — e.g. *order → inventory*
  ("reserve stock") and *order → product* ("authoritative price").
- **Asynchronous calls** (side effects) use Kafka events — e.g. *order placed*
  triggers inventory and email reactions without blocking checkout.

---

## 7. Modules

| # | Module | Responsibility |
|---|---|---|
| 1 | Discovery Server | Service registry (Eureka) |
| 2 | Config Server | Centralised configuration |
| 3 | API Gateway | Single entry point, request routing |
| 4 | User Service | Registration, login, JWT issuance, accounts |
| 5 | Product Service | Catalogue CRUD + full-text search (NoSQL) |
| 6 | Inventory Service | Stock levels, reservation (optimistic locking) |
| 7 | Order Service | Order placement and lifecycle, circuit breaker |
| 8 | Payment Service | Stripe charges/refunds, idempotent payments |
| 9 | Cart Service | Per-user cart with price snapshotting |
| 10 | Review Service | Product reviews and rating summaries |
| 11 | Recommendation Service | Co-purchase / trending recommendations |
| 12 | Notification Service | Event-driven email notifications |
| 13 | Admin Service | Dashboard aggregation (stats, orders, users) |

---

## 8. Methodology

An **incremental, service-by-service** approach was followed: infrastructure backbone
(discovery, config, gateway) first; then identity (user/auth) to enable testing of
secured endpoints; then catalogue and inventory; then the order–payment money path;
then engagement features (reviews, recommendations, notifications) and the admin
dashboard; finally the React front end and containerised deployment. Each service was
developed, containerised, and integration-tested against the event bus and gateway.

---

## 9. Key Design Highlights

- **Reserve-then-confirm orders:** stock is reserved synchronously (preventing
  oversell) while order status advances asynchronously via events (eventual
  consistency where acceptable).
- **Server-authoritative pricing:** order totals are recomputed from the catalogue;
  client-supplied prices are ignored, preventing price-tampering.
- **Idempotent payments:** Stripe charges carry an idempotency key so retries never
  double-charge.
- **Concurrency safety:** inventory uses optimistic locking (version column) so two
  customers cannot both buy the last unit.
- **Fault tolerance:** a circuit breaker around the inventory dependency degrades
  gracefully and is tuned to ignore business outcomes (out-of-stock) versus
  infrastructure failures.
- **Single-origin front end:** the SPA is served behind a reverse proxy that forwards
  `/api` to the gateway, eliminating CORS.

---

## 10. Technology Stack

| Layer | Technologies |
|---|---|
| Language / Runtime | Java 25, TypeScript 5 |
| Backend framework | Spring Boot, Spring Cloud (Gateway, Eureka, Config, OpenFeign), Spring Security, Spring Data JPA & MongoDB, Spring Kafka |
| Resilience / Auth / Pay | Resilience4j (circuit breaker), JJWT (JWT), Stripe SDK, BCrypt |
| Databases | MySQL (transactional), MongoDB (catalogue + search) |
| Messaging | Apache Kafka + Zookeeper |
| Frontend | React 18, React Router, Vite, Axios |
| Build / Deploy | Maven (multi-module), Docker, Docker Compose, nginx |
| Dev tooling | MailHog (SMTP), Actuator, Micrometer/Prometheus |

---

## 11. Hardware & Software Requirements

**Software:** JDK 25, Node.js, Maven, Docker & Docker Compose, Git; any modern browser.

**Development hardware:** ≥ 8 GB RAM (16 GB recommended), multi-core CPU, ~10 GB free
disk.

**Cloud deployment (single-host):** one t3.large (8 GB) EC2 instance running the full
containerised stack; data persisted on attached storage volumes.

---

## 12. Expected Outcomes

- A working, end-to-end online shopping platform exercising all retail flows.
- A demonstrable, resilient microservices architecture with documented design
  decisions, workflows, and deployment instructions.
- A containerised stack runnable with a single command locally and deployable to AWS.

---

## 13. Future Scope

- **Cloud-native deployment:** migrate to managed services — ECS/EKS for containers,
  RDS for SQL, managed Kafka, behind a load balancer with auto-scaling.
- **Observability:** distributed tracing (OpenTelemetry) and dashboards (Grafana).
- **Reliability:** outbox/saga pattern to eliminate reservation edge cases; payment
  webhooks for reconciliation.
- **Features:** wishlist, coupons/discounts, multi-tenancy, ML-based recommendations,
  and a mobile client.

---

## 14. Conclusion

ShopSphere demonstrates how a non-trivial e-commerce system can be engineered as a set
of small, independently deployable services that collaborate over synchronous and
asynchronous channels, with deliberate attention to security, correctness, and fault
tolerance. Beyond functioning as a storefront, it serves as a practical study of
distributed-systems patterns — service discovery, API gateways, event-driven design,
polyglot persistence, circuit breaking, and idempotency — and of the operational
realities of building, debugging, and deploying such a system.

---

### Reference documents
- System map: [`WORKFLOW.md`](./WORKFLOW.md)
- Engineering narrative & war stories: [`STORY.md`](./STORY.md)
- Component/feature notes: [`NOTES.md`](./NOTES.md)
- Per-service detail: [`services/`](./services)
- Deployment: [`AWS_DEPLOYMENT.md`](./AWS_DEPLOYMENT.md)
