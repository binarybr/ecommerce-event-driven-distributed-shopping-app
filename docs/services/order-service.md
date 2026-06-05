# order-service

**Port:** 8083 · **DB:** MySQL `order_service` · **Resilience:** Resilience4j circuit breaker

Places and tracks orders. The backend models **one product per order** — the frontend
places one order per cart line. Synchronous Feign calls to inventory, product, and user
services; publishes order events to Kafka.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/orders` | ADMIN/CUSTOMER | Place an order |
| GET | `/api/orders` | ADMIN/CUSTOMER | List orders |
| GET | `/api/orders/{id}` | ADMIN/CUSTOMER | Get one order |
| PUT | `/api/orders/{id}/status?status=` | ADMIN | Change status (used by admin-service) |
| DELETE | `/api/orders/{id}` | ADMIN | Cancel/delete (emits `order-cancelled`) |

## Place-order flow (`placeOrder`, wrapped by `@CircuitBreaker("inventory")`)

1. **Reserve stock** — Feign `POST /api/inventory/reserve?productId&quantity`
   (forwards caller's JWT). Despite the client method name `isInStock()`, this
   **decrements** stock. Returns `inStock=true/false`.
2. If not in stock → `ProductOutOfStockException` (HTTP 400).
3. **Authoritative pricing** (`resolveAuthoritativePrice`) — Feign
   `GET /api/products/{id}`, compute `price = unitPrice * quantity`. **The
   client-supplied price is ignored** (prevents forged-price under-pay). Missing
   product → rejected.
4. Build `Order`, set `orderNumber` (UUID), `status=PLACED`, `createdAt`.
5. Save to MySQL.
6. Best-effort fetch customer email (Feign `GET /api/users/{id}`).
7. Publish Kafka **`order-placed`** (consumed by inventory + notification).

## Circuit breaker

Instance `inventory`: sliding window 10, 50% failure threshold, 10s open.
**`ignore-exceptions`** = `ProductOutOfStockException`, `OrderNotFoundException` — these
business outcomes must not trip the breaker (otherwise a few out-of-stock attempts would
reject all orders). The `fallbackMethod` re-throws business exceptions and only converts
genuine infra failures into "Inventory service unavailable".

> Known trade-off (commented in code): stock is reserved *before* the order row is
> saved, so a save failure could leak the reservation. The cancel path emits
> `order-cancelled` to compensate.

## Events

- **Produces:** `order-placed`, `order-cancelled`
- **Consumes:** `inventory-reserved`, `inventory-failed` (via `InventoryEventConsumer`)

## Key files

`OrderServiceImpl`, `OrderController`, `client/InventoryClient` · `client/ProductClient`
· `client/UserClient`, `config/FeignClientConfig` (JWT forwarding), `OrderMapper`,
`handler/GlobalExceptionHandler`, `application.yaml` (resilience4j).
