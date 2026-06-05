# review-service

**Port:** 8088 · **DB:** MySQL `review_service`

Product reviews and rating summaries. One review per (user, product), enforced by a
unique constraint. Listing reviews is public; writing requires authentication.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| GET | `/api/reviews/product/{productId}` | public | Paged reviews for a product (newest first) |
| GET | `/api/reviews/product/{productId}/summary` | public | Average rating + count |
| GET | `/api/reviews/me` | authenticated | Caller's own reviews |
| POST | `/api/reviews` | authenticated | Create a review |
| PUT | `/api/reviews/{id}` | authenticated (owner) | Update own review |
| DELETE | `/api/reviews/{id}` | owner or ADMIN | Delete a review |

*(Paths per `ReviewController`.)*

## Behaviour

- **One review per user/product:** enforced by a `(user_email, product_id)` unique
  constraint. A duplicate insert throws `DataIntegrityViolationException`, translated
  to `ReviewException("You have already reviewed this product. Use PUT to update.")`.
- **Ownership:** update/delete check `review.userEmail == caller`; `productId` cannot
  be changed on update. Admins may delete any review.
- **Summary:** `averageRatingForProduct` + `countByProductId`, rounded to 2 decimals.

## Events

- **Produces:** `review-submitted`
- **Consumes:** none

## Key files

`ReviewServiceImpl`, `ReviewController`, `ReviewMapper`, `ReviewRepository`,
`entity/Review`.
