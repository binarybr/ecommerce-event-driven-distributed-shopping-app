# Inventory Service — Implementation

**Status:** ✅ Complete & verified | **Port:** 8082 | **DB:** MySQL `inventory_service`

## Purpose
Tracks stock levels and handles reservation/release with optimistic locking.

## Components
| Layer | Class(es) |
|-------|-----------|
| Entity | `Inventory` (`@Version` for optimistic locking) |
| Repository | `InventoryRepository` (findByProductId) |
| DTOs | `InventoryRequestDto`, `InventoryResponseDto` |
| Security | `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig` |
| Config | `KafkaConfig` (topics: `inventory-reserved`, `inventory-failed`) |
| Consumers | `OrderPlacedConsumer` (emits reserved event), `OrderCancelledConsumer` (restock) |
| Service | `InventoryService` / `InventoryServiceImpl` |
| Controller | `InventoryController` |
| Mapper | `InventoryMapper` |

## Endpoints
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| GET | `/api/inventory?productId&quantity` | CUSTOMER/ADMIN | **Read-only** availability check |
| POST | `/api/inventory/reserve?productId&quantity` | CUSTOMER/ADMIN | Reserve (decrement); called by order-service via Feign |
| POST | `/api/inventory` | ADMIN | Add/replenish stock |
| GET | `/api/inventory/all` | ADMIN | List all (dashboard) |

## Events
- **Consumes** `order-placed` (publishes `inventory-reserved`), `order-cancelled` (releases stock).
- **Publishes** `inventory-reserved`, `inventory-failed`.

## Key design decisions
- **Check vs reserve split:** `GET` is read-only (safe/idempotent); the decrementing logic lives in `POST /reserve` (this was a fixed REST anti-pattern — GET used to mutate stock).
- **Optimistic locking:** the `@Version` column detects concurrent reserves, preventing oversell.
- **Stock release:** `releaseStock` is invoked from the `order-cancelled` consumer so cancelled/deleted orders don't leak reserved units (fixed bug).
- **Reserve allows CUSTOMER:** order-service forwards the placing customer's JWT, so `/reserve` permits CUSTOMER; replenish (`POST /api/inventory`) is ADMIN-only.

## Notes / follow-ups
- No auto-seed on `product-created`; stock must be added explicitly by an admin.
