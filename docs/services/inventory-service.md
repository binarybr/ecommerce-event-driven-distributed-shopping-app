# inventory-service

**Port:** 8082 · **DB:** MySQL `inventory_service`

Authoritative source of orderable stock. A product's catalog `stock` field is metadata;
**real availability lives here**. One `inventory` row per product (`productId`,
`quantity`, `@Version` for optimistic locking).

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/api/inventory?productId&quantity` | ADMIN/CUSTOMER | Read-only availability check |
| POST | `/api/inventory/reserve?productId&quantity` | ADMIN/CUSTOMER | **Reserve** (decrement) stock for an order |
| POST | `/api/inventory` | ADMIN | Add/replenish stock (upsert: increments if row exists) |
| GET | `/api/inventory/all` | ADMIN | List all inventory rows (dashboard) |

## How stock moves

- **Reserve** (`reserveStock`, called by order-service during placement): if
  `quantity >= requested`, decrement and return `inStock=true`; else no change,
  `inStock=false`. `@Version` guards concurrent reserves. If **no row exists** for the
  product → `InventoryNotFoundException` (404) — so every product must have a row.
- **Release** (`releaseStock`): adds units back; triggered when an order is cancelled.
- **Add** (`addInventory`): upsert — increments an existing row or creates a new one.

## Auto-seeding new products (`ProductCreatedConsumer`)

Listens to Kafka **`product-created`**. When a product is created (seeded or via the
admin form), it creates an inventory row seeded with the event's `stock` value, so the
product is orderable immediately. Idempotent — skips if a row already exists.

> This is why `ProductCreatedEvent` carries a `stock` field: without it, rows were
> created at quantity 0 and every order failed with "out of stock".

## Events

- **Produces:** `inventory-reserved`, `inventory-failed`
- **Consumes:** `product-created` (seed row), `order-placed` (`OrderPlacedConsumer`
  re-emits `inventory-reserved`; does **not** decrement again — order-service already
  reserved synchronously), `order-cancelled` (release stock)

## Key files

`InventoryServiceImpl`, `InventoryController`, `consumer/ProductCreatedConsumer`,
`consumer/OrderPlacedConsumer`, `consumer/OrderCancelledConsumer`, `entity/Inventory`,
`SecurityConfig`.
