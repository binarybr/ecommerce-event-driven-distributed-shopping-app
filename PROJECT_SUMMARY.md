# PROJECT SUMMARY

This project is an **online shopping microservices application** built with
Spring Boot 4 / Spring Cloud. It is **complete, security-hardened, and verified
end-to-end** via a repeatable regression suite.

> History: this codebase started as a ~60% skeleton with several critical bugs.
> All of those have been fixed, four major features were added (search,
> reviews, recommendations, admin), and JWT/RBAC security was applied across
> all write paths. Per-service detail lives in each service's `IMPLEMENTATION.md`.

## 📊 SERVICES (13 total)

### Infrastructure (3)
| Service | Port | Role |
|---------|------|------|
| discovery-server | 8761 | Eureka service registry |
| config-server | 8888 | Centralized configuration |
| api-gateway | 8080 | Reactive (WebFlux) gateway; routes `/api/**`; Basic-auth on actuator |

### Business (10)
| Service | Port | DB | Highlights |
|---------|------|-----|-----------|
| user-service | 8085 | MySQL | Registration, login, **JWT issuer**, RBAC |
| product-service | 8081 | MongoDB | Catalog + **full-text search** |
| inventory-service | 8082 | MySQL | Stock, optimistic locking, reserve/release |
| order-service | 8083 | MySQL | Order saga, Feign, circuit breaker |
| payment-service | 8086 | MySQL | Stripe + FAILED audit trail |
| cart-service | 8087 | MySQL | Cart, price snapshot |
| notification-service | 8084 | MySQL | Email/SMS/push, retry topics |
| review-service | 8088 | MySQL | Ratings & reviews ⭐ |
| recommendation-service | 8089 | MySQL | Co-purchase engine 💡 |
| admin-service | 8090 | — | Feign aggregator dashboard 🛠️ |

Plus infra components: **Kafka + Zookeeper**, **MailHog** (SMTP test).

## 🧱 TECH STACK
- Spring Boot 4.0.6, Spring Cloud 2025.1.1, Java 25, Maven multi-module
- Eureka (discovery), Spring Cloud Gateway (reactive), OpenFeign (sync calls)
- Resilience4j (circuit breaker), Apache Kafka (events)
- MySQL 8 (6 services) + MongoDB 7 (product); JPA/Hibernate + Spring Data Mongo
- Spring Security + JWT (JJWT), BCrypt, `@PreAuthorize` RBAC
- Stripe SDK (payments), JavaMail/MailHog (email)
- Docker Compose orchestration (18 containers)

## 🔐 SECURITY MODEL
- **user-service** mints JWTs (subject=email, claims: userId, role); all services validate with a shared HMAC secret.
- **Gateway** passes `/api/**` through; downstream services enforce auth/roles.
- **Public:** product browse/search, review reads, recommendation product/trending.
- **CUSTOMER/ADMIN:** cart, orders, payments, reviews (write), personalized recs.
- **ADMIN only:** product/inventory writes, order status changes, all `/api/admin/**`.
- **Service-to-service:** order-service and admin-service forward the caller's JWT on Feign calls.

## 🔄 EVENT FLOWS (Kafka topics)
```
user-registered      user → notification (welcome email)
order-placed         order → inventory (reserve) + notification + recommendation
inventory-reserved   inventory → order (status CONFIRMED)
inventory-failed     inventory → order (status CANCELLED)
order-cancelled      order → inventory (restock)
payment-processed    payment → notification (success email)
payment-failed       payment → notification (failure email)
payment-refunded     payment → (audit)
review-submitted     review → recommendation (interaction signal)
item-added-to-cart   cart → (analytics)
cart-cleared         cart → (analytics)
product-created      product → (no consumer yet)
```

## ✅ COMPLETION STATUS
```
All 13 services .................. ✅ Complete & running
4 advanced features .............. ✅ Search, Reviews, Recommendations, Admin
5 critical/high review fixes ..... ✅ Fixed & verified
Documentation .................... ✅ Class/field/why comments + per-service IMPLEMENTATION.md
End-to-end regression ............ ✅ 14-section seed+test script passes
```

### Critical/high fixes applied (previously open bugs)
1. Cart clear now deletes `cart_item` rows (`orphanRemoval=true`).
2. Payment FAILED records persist (`@Transactional(noRollbackFor=PaymentException.class)`).
3. Inventory check/reserve split (GET no longer mutates stock; `POST /reserve` does).
4. Inventory released on order cancel/delete via `order-cancelled` event.
5. JWT + RBAC added to product / order / inventory (previously unauthenticated).

## 📚 DOCUMENTATION MAP
- **Per-service detail:** `business-services/<service>/IMPLEMENTATION.md`
- **Run locally:** `RUNNING_LOCAL_GUIDE.md`
- **This file:** high-level architecture & status
- **Index:** `DOCUMENTATION_INDEX.md`

## 🧪 RUNNING & TESTING
```powershell
# Build everything
.\mvnw.cmd clean install -DskipTests

# Start the full stack
docker compose -f deployment\docker\docker-compose.yml up -d

# Gateway: http://localhost:8080  | Eureka: http://localhost:8761  | MailHog: http://localhost:8025
```
A full seed + regression script (registers customers, creates products, places
orders, payments, reviews, checks recommendations + admin dashboard + all data
stores) is documented in `RUNNING_LOCAL_GUIDE.md`.

## 🟡 OPTIONAL BACKLOG (non-blocking)
- `product-created` → inventory auto-seed consumer
- Order PLACED-stuck reconciliation/timeout
- Refund-amount validation; `BigDecimal` for money
- Pagination on admin list endpoints
- Consolidate the (currently redundant) async inventory-failed saga path

---
**Status:** ✅ Complete — 13 services, hardened, documented, regression-verified.
