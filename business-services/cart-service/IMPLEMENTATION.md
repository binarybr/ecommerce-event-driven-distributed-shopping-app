# Cart Service — Implementation

**Status:** ✅ Complete & verified | **Port:** 8087 | **DB:** MySQL `cart_service`

## Purpose
Manages per-user shopping carts. Fetches live prices from product-service via
Feign and snapshots them onto cart lines.

## Components
| Layer | Class(es) |
|-------|-----------|
| Entities | `Cart` (one per user, unique userId), `CartItem` (price snapshot) |
| Repositories | `CartRepository`, `CartItemRepository` |
| DTOs | `AddToCartDto`, `CartResponseDto`, `CartItemResponseDto`, `ProductDto` |
| Feign | `ProductClient` (price lookup) |
| Security | `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig` |
| Service | `CartService` / `CartServiceImpl` |
| Controller | `CartController` |
| Kafka | `KafkaConfig` (topics: `item-added-to-cart`, `cart-cleared`) |

## Endpoints (all require CUSTOMER or ADMIN; user identity = JWT subject)
| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/cart/items` | Add item (merges quantity if product already present) |
| PUT | `/api/cart/items/{itemId}` | Update quantity |
| DELETE | `/api/cart/items/{itemId}` | Remove a line |
| GET | `/api/cart` | View cart (total + item count) |
| DELETE | `/api/cart` | Clear cart |

## Events
- **Publishes** `item-added-to-cart`, `cart-cleared` (analytics / future consumers).

## Key design decisions
- **Identity = JWT email:** the cart is keyed by `authentication.getName()` (the JWT subject), never a request-body field.
- **Price snapshot:** the catalog price is fetched once via Feign and stored on the `CartItem`, so the cart total is stable even if the product price later changes.
- **orphanRemoval:** `Cart.items` uses `cascade=ALL, orphanRemoval=true` — `clearCart()`/`removeItem()` actually DELETE the `cart_item` rows (this was a fixed bug).
- **Single Feign call:** `addItem` captures the price in both branches to avoid a redundant product-service call when publishing the Kafka event.

## Notes / follow-ups
- Events are published on add/clear but not on update/remove — could be made consistent.
