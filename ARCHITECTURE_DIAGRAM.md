# SERVICE ARCHITECTURE & DEPENDENCIES

Current architecture of the Online Shopping microservices platform — **13
services** (3 infrastructure + 10 business), event-driven via Kafka, with
JWT/RBAC security and polyglot persistence.

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

## 🔐 Security Boundaries

| Caller | Boundary | Mechanism |
|--------|----------|-----------|
| Client → Gateway | `/api/**` open; actuator Basic-auth | Gateway WebFlux security |
| Client → Service | per-endpoint role checks | JWT Bearer + `@PreAuthorize` |
| Service → Service | identity propagation | Feign interceptor forwards `Authorization` |

- **Public:** product browse/search, review reads, recommendation product/trending.
- **CUSTOMER/ADMIN:** cart, orders, payments, reviews (write), personalized recs, inventory reserve.
- **ADMIN only:** product/inventory writes, order status changes, `/api/admin/**`.

## 🔗 Synchronous Calls (OpenFeign + Eureka load-balancing)

```
order-service ──► inventory-service   POST /api/inventory/reserve   (reserve stock)
order-service ──► user-service        GET  /api/users/{id}          (customer email)
cart-service  ──► product-service     GET  /api/products/{id}       (live price)
admin-service ──► user/product/order/inventory/notification         (dashboard aggregation)
```
All of the above forward the caller's JWT; order-service wraps the inventory
call in a **Resilience4j circuit breaker** with a graceful fallback.

## 📨 Event-Driven Flows (Apache Kafka)

```
user-registered     user        → notification (welcome email)
                                → recommendation (interaction)
order-placed        order       → inventory (emit reserved/failed)
                                → notification (order email)
                                → recommendation (PURCHASE interaction)
inventory-reserved  inventory   → order (status → CONFIRMED)
inventory-failed    inventory   → order (status → CANCELLED)
order-cancelled     order       → inventory (RESTOCK reserved units)
payment-processed   payment     → notification (success email)
payment-failed      payment     → notification (failure email)
payment-refunded    payment     → (audit)
review-submitted    review      → recommendation (REVIEW interaction)
item-added-to-cart  cart        → (analytics / future)
cart-cleared        cart        → (analytics / future)
product-created     product     → (no consumer yet)
```

## 🛒 End-to-End Order Saga

```
1. Client POST /api/orders (Bearer JWT)
2. Gateway → order-service (validates CUSTOMER/ADMIN)
3. order-service ──Feign──► inventory /reserve   (decrement, optimistic lock)
        └─ insufficient → ProductOutOfStockException (order not created)
4. order saved (status PLACED) → publish order-placed
5. inventory-service consumes order-placed → publish inventory-reserved
6. order-service consumes inventory-reserved → status CONFIRMED
   (cancel/delete later → order-cancelled → inventory restock)
```

## 🗄️ Persistence

| Store | Services |
|-------|----------|
| MySQL `user_service` | user |
| MongoDB `product_service` | product (text-indexed for search) |
| MySQL `inventory_service` | inventory (`@Version` optimistic lock) |
| MySQL `order_service` | order |
| MySQL `payment_service` | payment |
| MySQL `cart_service` | cart, cart_item |
| MySQL `notification_service` | notifications |
| MySQL `review_service` | reviews |
| MySQL `recommendation_service` | user_interactions |
| (none) | admin — pure aggregator |

## 🧱 Cross-Cutting Concerns

| Concern | Implementation |
|---------|---------------|
| Service discovery | Eureka (`@EnableDiscoveryClient`) |
| Config | Spring Cloud Config server |
| Gateway | Spring Cloud Gateway (reactive) |
| Sync RPC | OpenFeign + Spring Cloud LoadBalancer |
| Resilience | Resilience4j circuit breaker (order→inventory) |
| Async | Apache Kafka (12 topics) + retry topics/DLT (notification) |
| Security | Spring Security + JWT (JJWT), BCrypt, `@PreAuthorize` |
| Concurrency | JPA `@Version` optimistic locking (inventory) |
| Payments | Stripe SDK (idempotency keys) |
| Email | JavaMail → MailHog (dev) |

---
**Status:** ✅ All services implemented, secured, and verified end-to-end.
See per-service `business-services/<service>/IMPLEMENTATION.md` for detail.
