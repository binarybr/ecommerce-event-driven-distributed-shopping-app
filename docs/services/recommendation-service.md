# recommendation-service

**Port:** 8089 · **DB:** MySQL `recommendation_service`

Serves product recommendations from recorded user–product interactions using simple
SQL-based strategies (no ML model). Read-only at the service layer.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/api/recommendations/product/{productId}?limit` | public | "Customers also bought" (co-purchase) |
| GET | `/api/recommendations/user/{email}?limit` | authenticated | Personalised for a user |
| GET | `/api/recommendations/trending?limit` | public | Trending products |

*(Paths per the controller; `limit` is clamped to 1–50, default 5.)*

## Strategies (native queries in `UserInteractionRepository`)

- **co-purchase** — products frequently bought alongside the given product.
- **personalized** — products linked to the user's past interactions.
- **trending** — most-interacted products overall.

Each query returns `Object[]{ productId, score }`; `RecommendationServiceImpl.toDtos`
maps rows to `RecommendationDto` and wraps them in a `RecommendationResponse` tagged
with the `strategy` used. The frontend resolves each `productId` to a full product via
product-service (best-effort).

## Events

- **Produces:** none
- **Consumes:** interaction-producing events may populate the interaction table
  (e.g. orders/reviews) depending on configuration.

## Key files

`RecommendationServiceImpl`, `UserInteractionRepository`, `dto/RecommendationResponse`,
`dto/RecommendationDto`.
