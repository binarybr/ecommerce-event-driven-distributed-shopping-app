# Product Service — Implementation

**Status:** ✅ Complete & verified | **Port:** 8081 | **DB:** MongoDB `product_service`

## Purpose
Product catalog (CRUD) plus **full-text search** with weighted relevance,
filtering, sorting, and pagination — backed by MongoDB text indexes.

## Components
| Layer | Class(es) |
|-------|-----------|
| Entity | `Product` (`@TextIndexed` on name/brand/tags/description with weights) |
| Repositories | `ProductRepository` (Mongo), `ProductSearchRepository` (custom criteria) |
| DTOs | `ProductRequestDto`, `ProductResponseDto`, `SearchRequestDto`, `SearchResponseDto` |
| Security | `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig` |
| Config | `MongoConfig` (explicit `MongoClient`), `KafkaConfig` (topic `product-created`) |
| Services | `ProductService`/`Impl`, `SearchService` |
| Controller | `ProductController` |
| Mapper | `ProductMapper` |

## Endpoints
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| GET | `/api/products` | public | List all |
| GET | `/api/products/{id}` | public | By id |
| GET | `/api/products/search` | public | q, category, brand, tag, minPrice, maxPrice, inStockOnly, sortBy, sortDir, page, size |
| POST | `/api/products` | ADMIN | Create |
| PUT | `/api/products/{id}` | ADMIN | Update |
| DELETE | `/api/products/{id}` | ADMIN | Delete |

## Events
- **Publishes** `product-created`.

## Key design decisions
- **Relevance ranking:** `@TextIndexed` weights (name=5, brand=3, tags=2, description=1) drive `$text` score ordering; `ProductSearchRepository` uses `TextQuery.sortByScore()`.
- **Auto-index creation:** `MongoConfig.autoIndexCreation()` returns true so the text index is built on startup (otherwise `$text` queries fail with IndexNotFound).
- **Explicit MongoClient:** `MongoConfig` builds the `MongoClient` from `spring.data.mongodb.uri` directly, working around a Spring Boot 4 auto-config quirk that defaulted to localhost.
- **Public reads, ADMIN writes:** browsing/search needs no token; catalog mutations require ADMIN.

## Notes / follow-ups
- `product-created` currently has no consumer — a natural enhancement is inventory-service auto-seeding a 0-stock row.
