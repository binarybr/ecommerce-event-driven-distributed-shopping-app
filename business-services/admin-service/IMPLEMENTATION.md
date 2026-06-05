# Admin Service — Implementation

**Status:** ✅ Complete & verified | **Port:** 8090 | **DB:** none (pure aggregator)

## Purpose
Admin dashboard backend. Holds **no local state** — every endpoint composes
data by calling other services over Feign. All endpoints require **ROLE_ADMIN**.

## Components
| Layer | Class(es) |
|-------|-----------|
| Feign clients | `UserClient`, `ProductClient`, `OrderClient`, `InventoryClient`, `NotificationClient` |
| External DTOs | `UserDto`, `ProductDto`, `OrderDto`, `InventoryDto`, `NotificationDto`, `ProductRequestDto` |
| Internal DTOs | `AdminStatsDto`, `BulkImportResponseDto` |
| Security | `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig` |
| Config | `FeignClientConfig` (forwards Authorization header) |
| Service | `AdminService` / `AdminServiceImpl` (graceful degradation) |
| Controller | `AdminController` (`@PreAuthorize("hasRole('ADMIN')")` at class level) |

## Endpoints (all ADMIN)
| Method | Path | Notes |
|--------|------|-------|
| GET | `/api/admin/stats` | Aggregated totals (users, products, orders, revenue, notifications, by-status, out-of-stock) |
| GET | `/api/admin/users` | All users |
| GET | `/api/admin/orders` | All orders |
| GET | `/api/admin/orders/recent?limit` | Recent orders |
| PUT | `/api/admin/orders/{id}/status?status` | Update order status |
| GET | `/api/admin/inventory/low-stock?threshold` | Low-stock products |
| POST | `/api/admin/products/bulk-import` | Best-effort bulk product create |
| GET | `/api/admin/notifications` | Notification audit log |
| GET | `/api/admin/notifications/failed` | Failed notifications |

## Key design decisions
- **Aggregator pattern:** no DB; all data via Feign to user/product/order/inventory/notification services.
- **Feign JWT forwarding:** `FeignClientConfig` forwards the ADMIN token so downstream `@PreAuthorize` checks pass (e.g. user-service `GET /api/users`, order-service status update).
- **Graceful degradation:** `safeFetch` wraps each downstream call so one failing service yields empty/zero rather than crashing the whole dashboard.
- **Revenue excludes CANCELLED** orders; computed from `price * quantity`.

## Notes / follow-ups
- List endpoints load full result sets (no pagination yet) — fine at current scale.
