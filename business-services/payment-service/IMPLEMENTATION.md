# Payment Service — Implementation

**Status:** ✅ Complete & verified | **Port:** 8086 | **DB:** MySQL `payment_service`

## Purpose
Processes payments via the **Stripe** SDK and maintains a full audit trail of
every attempt (both COMPLETED and FAILED), publishing payment lifecycle events
to Kafka.

## Components
| Layer | Class(es) |
|-------|-----------|
| Entity | `Payment` (status, transactionId unique, errorMessage, completedAt) |
| Repository | `PaymentRepository` (findByUserId) |
| DTOs | `PaymentRequestDto`, `PaymentResponseDto`, `RefundRequestDto` |
| Security | `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig` |
| Config | `StripeConfig` (sets global `Stripe.apiKey`) |
| Service | `PaymentService` / `PaymentServiceImpl` |
| Controller | `PaymentController` |
| Kafka | `KafkaConfig` (topics: `payment-processed`, `payment-failed`, `payment-refunded`) |

## Endpoints (all require CUSTOMER or ADMIN)
| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/payments` | Create Stripe charge; persist COMPLETED/FAILED |
| GET | `/api/payments/{id}` | Payment details |
| GET | `/api/payments/history` | Caller's payment history |
| POST | `/api/payments/{id}/refund` | Refund a COMPLETED payment |

## Events
- **Publishes** `payment-processed` (success), `payment-failed` (Stripe error), `payment-refunded` → notification-service emails the customer.

## Key design decisions
- **Idempotency:** each charge uses `idempotency_key = userId + orderId`, so a retried request can't double-charge.
- **FAILED-record audit:** the class is `@Transactional(noRollbackFor = PaymentException.class)` — when Stripe fails we persist a FAILED `Payment` row *then* rethrow; without `noRollbackFor` that audit row would be rolled back.
- **Money to Stripe:** sent as integer minor units (`amount * 100`).
- **Refund guard:** only `COMPLETED` payments are refundable.

## Notes / follow-ups
- Refund amount is **not yet validated** against the original charge — a known follow-up.
- Money is modeled as `Double`; `BigDecimal` would be safer for production accounting.
- Requires a real `STRIPE_API_KEY` (`sk_test_...`) to actually succeed; the dev default fails fast and produces FAILED audit rows.
