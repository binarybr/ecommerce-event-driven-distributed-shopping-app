# product-service

**Port:** 8081 · **DB:** MongoDB `product_service`

The product catalog. MongoDB suits the flexible product schema and powers full-text
search via text indexes. Catalog reads are **public**; writes require **ADMIN**.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/api/products` | public | List all products |
| GET | `/api/products/{id}` | public | One product (used by order & cart via Feign) |
| GET | `/api/products/search?q&category&brand&minPrice&maxPrice&sortBy&sortDir&page&size` | public | Full-text + filter search |
| POST | `/api/products` | ADMIN | Create product |
| PUT | `/api/products/{id}` | ADMIN | Update product |
| DELETE | `/api/products/{id}` | ADMIN | Delete product |

## Document & search

`Product` (collection `products`) is text-indexed with field weights:
`name`(5) > `brand`(3) > `tags`(2) > `description`(1); `category`/`sku` are simple
indexes for equality filters. `SearchService` runs `$text` + filter queries with
relevance/price/name/createdAt sorting and pagination.

## Create flow

1. Map DTO → `Product`, persist to MongoDB.
2. Publish Kafka **`product-created`** (carrying `productId`, `name`, `price`,
   **`stock`**) → inventory-service seeds an orderable inventory row.

## Demo seeding (`config/DataSeeder`)

On first boot, if the collection is empty, seeds **12 demo products** (with images,
tags, stock) and publishes a `product-created` event for each. Idempotent; uses
`@CacheEvict` so the read cache reflects the seed.

## Events

- **Produces:** `product-created`
- **Consumes:** none

## Key files

`ProductServiceImpl`, `SearchService`, `ProductController`, `entity/Product`,
`config/DataSeeder`, `repository/ProductRepository`, `SecurityConfig`.
