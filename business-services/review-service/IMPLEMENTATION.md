# Review Service — Implementation

**Status:** ✅ Complete & verified | **Port:** 8088 | **DB:** MySQL `review_service`

## Purpose
Product reviews and star ratings, with average-rating aggregation. One review
per user per product.

## Components
| Layer | Class(es) |
|-------|-----------|
| Entity | `Review` (unique `(user_email, product_id)`, rating 1-5) |
| Repository | `ReviewRepository` (paged by product, average aggregate) |
| DTOs | `ReviewRequestDto`, `ReviewResponseDto`, `ReviewSummaryDto` |
| Security | `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig` |
| Service | `ReviewService` / `ReviewServiceImpl` |
| Controller | `ReviewController` |
| Mapper | `ReviewMapper` |
| Kafka | `KafkaConfig` (topic `review-submitted`) |

## Endpoints
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/api/reviews` | CUSTOMER/ADMIN | Create (1 per user/product) |
| PUT | `/api/reviews/{id}` | CUSTOMER/ADMIN | Update own review |
| DELETE | `/api/reviews/{id}` | CUSTOMER/ADMIN | Delete own (or any, if ADMIN) |
| GET | `/api/reviews/user/me` | CUSTOMER/ADMIN | My reviews |
| GET | `/api/reviews/product/{productId}` | public | Paged reviews for a product |
| GET | `/api/reviews/product/{productId}/summary` | public | avg rating + count |

## Events
- **Publishes** `review-submitted` → consumed by recommendation-service (interaction signal).

## Key design decisions
- **One review per user/product:** enforced by a DB unique constraint; a duplicate POST returns 409 Conflict.
- **Owner identity from JWT:** `ReviewMapper` stamps `userEmail`/`userId` from the token, not the request body — no spoofing.
- **Ownership checks:** update/delete verify the caller owns the review (ADMIN may delete any).
- **Public reads:** product reviews and rating summaries need no auth; writing requires a token.
- **Average via query:** `averageRatingForProduct` is a single aggregate query, rounded to 2 decimals; 0.0 when no reviews exist.
