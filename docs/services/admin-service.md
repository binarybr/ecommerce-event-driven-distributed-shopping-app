# admin-service

**Port:** 8090 · **DB:** none (pure aggregator)

Backs the admin dashboard. Holds no data of its own — it **fans out via Feign** to the
other services and aggregates the results. Every endpoint is `ADMIN`-only, enforced
both at the class level (`@PreAuthorize("hasRole('ADMIN')")`) and by the JWT filter.

## Endpoints (all under `/api/admin`, ADMIN only)

| Method | Path | Aggregates from |
|---|---|---|
| GET | `/stats` | users + products + orders + revenue + notifications + stock |
| GET | `/users` | user-service |
| GET | `/orders` | order-service |
| GET | `/orders/recent?limit` | order-service |
| PUT | `/orders/{id}/status?status` | order-service (status change) |
| GET | `/inventory/low-stock?threshold` | inventory-service |
| POST | `/products/bulk-import` | product-service (forwards ADMIN JWT) |
| GET | `/notifications` | notification-service |
| GET | `/notifications/failed` | notification-service |

## How it works

- Each call uses a Feign client targeting a downstream service (`lb://…` via Eureka).
- The caller's **ADMIN JWT is forwarded** so downstream `@PreAuthorize`/role checks
  pass (e.g. `order-service` PUT status is ADMIN-only; product bulk-import is
  ADMIN-only).
- `/stats` composes several downstream calls into one dashboard payload (totals,
  orders-by-status, revenue, out-of-stock count).

> Note: `order-service`'s `PUT /api/orders/{id}/status` is intended to be reached
> *through* admin-service, which guarantees the ADMIN role check upstream.

## Events

- **Produces:** none
- **Consumes:** none

## Key files

`AdminController`, the Feign clients, and the aggregation/service layer.
