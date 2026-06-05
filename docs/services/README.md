# Service workflow docs

Per-service workflow documentation. Start with the system map in
[`../WORKFLOW.md`](../WORKFLOW.md), then drill into a service below.

## Business services
- [user-service](./user-service.md) — auth, JWT issuance, accounts (8085)
- [product-service](./product-service.md) — catalog + search, MongoDB (8081)
- [inventory-service](./inventory-service.md) — stock & reservations (8082)
- [order-service](./order-service.md) — order placement & lifecycle (8083)
- [payment-service](./payment-service.md) — Stripe charges & refunds (8086)
- [cart-service](./cart-service.md) — per-user cart (8087)
- [review-service](./review-service.md) — reviews & ratings (8088)
- [recommendation-service](./recommendation-service.md) — recs (8089)
- [notification-service](./notification-service.md) — event-driven emails (8084)
- [admin-service](./admin-service.md) — dashboard aggregator (8090)

## Infrastructure
- [api-gateway](./api-gateway.md) — single entry point, routing (8080)
- [discovery-server](./discovery-server.md) — Eureka registry (8761)
- [config-server](./config-server.md) — central config (8888)

## Conventions used in these docs
- **Auth** column: `public` (no token), `authenticated` (any valid JWT),
  `CUSTOMER`/`ADMIN` (role required).
- **Events**: Kafka topics the service produces/consumes. The full topic map is in
  the overview.
- Synchronous cross-service calls are **Feign** over Eureka (`lb://…`); the caller
  forwards its `Authorization` header when the target needs auth.
