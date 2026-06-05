# Order Service — Implementation

**Status:** ✅ Complete & verified | **Port:** 8083 | **DB:** MySQL `order_service`

## Purpose
Places and tracks customer orders. Orchestrates the order saga: reserve stock
(sync, via Feign) → publish `order-placed` → confirm/cancel based on inventory
events.

## Components
| Layer | Class(es) |
|-------|-----------|
| Entity | `Order` (orderNumber UUID, status lifecycle) |
| Repository | `OrderRepository` (findByOrderNumber) |
| DTOs | `OrderRequestDto`, `OrderResponseDto`, `InventoryResponseDto`, `UserDto` |
| Feign | `InventoryClient` (reserve), `UserClient` (email lookup) |
| Resilience | Resilience4j `@CircuitBreaker(name="inventory")` + fallback |
| Security | `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig` |
| Config | `FeignClientConfig` (forwards Authorization header), `KafkaConfig` (`order-placed`) |
| Consumer | `InventoryEventConsumer` (inventory-reserved / inventory-failed) |
| Service | `OrderService` / `OrderServiceImpl` |
| Controller | `OrderController` |

## Endpoints
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/api/orders` | CUSTOMER/ADMIN | Place order (reserves stock first) |
| GET | `/api/orders` | CUSTOMER/ADMIN | List all |
| GET | `/api/orders/{id}` | CUSTOMER/ADMIN | By id |
| PUT | `/api/orders/{id}/status` | ADMIN | Change status (e.g. SHIPPED) |
| DELETE | `/api/orders/{id}` | ADMIN | Cancel/delete → restock |

## Events
- **Publishes** `order-placed` (→ inventory + notification), `order-cancelled` (→ inventory restock).
- **Consumes** `inventory-reserved` (→ status CONFIRMED), `inventory-failed` (→ status CANCELLED).

## Order lifecycle
`PLACED → CONFIRMED` (inventory reserved) — or `CANCELLED` (reservation failed / deleted).

## Key design decisions
- **Sync reserve via POST:** calls `POST /api/inventory/reserve` (not a GET) — reservation mutates stock, so it must be a non-idempotent verb.
- **Feign JWT forwarding:** `FeignClientConfig` copies the caller's `Authorization` header onto outbound calls so user-service/inventory-service `@PreAuthorize` checks pass.
- **Circuit breaker:** inventory calls are wrapped; if inventory-service is down the fallback returns a friendly error instead of cascading.
- **Restock on cancel:** `deleteOrder` publishes `order-cancelled`, which inventory-service consumes to release the reserved units.

## Notes / follow-ups
- The async `inventory-failed` path is largely redundant since shortages are caught synchronously during reserve; the saga could be consolidated to one model.
- An order can theoretically stay PLACED if the `inventory-reserved` event races the commit — a reconciliation job would close this gap.
