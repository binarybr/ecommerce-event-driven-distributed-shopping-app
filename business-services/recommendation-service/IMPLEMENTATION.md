# Recommendation Service — Implementation

**Status:** ✅ Complete & verified | **Port:** 8089 | **DB:** MySQL `recommendation_service`

## Purpose
Co-purchase recommendation engine ("customers who bought X also bought Y").
Builds a denormalized user-product interaction view from Kafka events and
serves recommendations via SQL self-joins — no external ML.

## Components
| Layer | Class(es) |
|-------|-----------|
| Entities | `UserInteraction` (unique `(user_email, product_id, type)`), `InteractionType` enum |
| Repository | `UserInteractionRepository` (3 native co-purchase queries) |
| DTOs | `RecommendationDto`, `RecommendationResponse` |
| Security | `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig` |
| Services | `InteractionService`/`Impl` (idempotent insert), `RecommendationService`/`Impl` |
| Controller | `RecommendationController` |
| Consumers | `OrderPlacedConsumer`, `ReviewSubmittedConsumer` |

## Endpoints
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| GET | `/api/recommendations/product/{productId}` | public | Co-purchase for a product |
| GET | `/api/recommendations/trending` | public | Top products by total weight |
| GET | `/api/recommendations/user/me` | CUSTOMER/ADMIN | Personalized for caller |

## Events consumed
| Topic | Interaction | Weight |
|-------|-------------|--------|
| `order-placed` | PURCHASE | 3.0 |
| `review-submitted` | REVIEW | 1.0 |

## Algorithm (co-purchase, weighted)
For a product X: find all users who interacted with X, then aggregate the OTHER
products those users interacted with, summing interaction weight, ordered DESC.
Personalized recs extend this over every product the user has, excluding items
they already have.

## Key design decisions
- **Weighted signals:** purchases (3.0) outrank reviews (1.0) in ranking.
- **Idempotent ingestion:** unique `(user_email, product_id, type)` makes Kafka at-least-once delivery safe — re-emitted events are no-ops.
- **Shared identity key:** interactions are keyed by email so PURCHASE (order) and REVIEW rows for the same user join correctly (order-service forwards the customer email).
- **SQL self-joins:** all ranking is done in three native queries — cheap and explainable.

## Notes / follow-ups
- Pure collaborative filtering; could be hybridized with content-based (category/tags) for cold-start users.
