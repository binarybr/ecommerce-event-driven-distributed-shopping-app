# cart-service

**Port:** 8087 · **DB:** MySQL `cart_service`

One cart per user (keyed by the JWT subject = email). Snapshots product price at
add-time so the cart total is stable even if the catalog price later changes.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/api/cart` | authenticated | Get (or lazily create) the caller's cart |
| POST | `/api/cart/items` | authenticated | Add item (merges qty if already present) |
| PUT | `/api/cart/items/{itemId}` | authenticated | Update quantity |
| DELETE | `/api/cart/items/{itemId}` | authenticated | Remove a line |
| DELETE | `/api/cart` | authenticated | Clear the cart |

*(Exact paths per `CartController`; identity always comes from `authentication.getName()`.)*

## Behaviour

- **Lazy creation:** first add creates the cart — no separate "create cart" call.
- **Merge:** adding a product already in the cart bumps its quantity instead of adding
  a duplicate line.
- **Price snapshot:** new lines fetch the current price once via Feign
  `GET /api/products/{id}` and store it on the `CartItem`. Existing lines reuse the
  snapshot (no second Feign call).
- **Transactions:** the class is `@Transactional`; `Cart.items` uses
  `orphanRemoval=true`, so removing/clearing items issues DELETEs on flush.
- **Product lookup errors:** a missing product surfaces as `FeignException.NotFound`
  → `CartException("Product not found")` (fixed from the never-matching
  `HttpClientErrorException.NotFound`).

## Events

- **Produces:** `item-added-to-cart`, `cart-cleared`
- **Consumes:** none

## Key files

`CartServiceImpl`, `CartController`, `client/ProductClient`, `entity/Cart` &
`entity/CartItem`, `dto/ProductDto`.
