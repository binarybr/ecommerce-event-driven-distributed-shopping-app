# payment-service

**Port:** 8086 · **DB:** MySQL `payment_service` · **External:** Stripe

Processes card payments via Stripe and records every attempt (success or failure).
Decoupled from orders — an order can exist unpaid and be paid later.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/payments` | authenticated | Charge a card for an order |
| GET | `/api/payments/{id}` | authenticated | Payment details |
| GET | `/api/payments/history` | authenticated | Caller's payment history |
| POST | `/api/payments/{id}/refund` | authenticated | Refund a COMPLETED payment |

## Charge flow (`processPayment`)

1. Build a Stripe `Charge`: `amount = total*100` (minor units), `currency`, `source`
   = `stripeToken`, `description`.
2. **Idempotency** — the key `userId-orderId` is sent as the **`Idempotency-Key`
   header** via `RequestOptions` (NOT a body param — Stripe rejects that). A retried
   request for the same order won't double-charge.
3. On success → save `Payment(status=COMPLETED, transactionId=charge.id)`, publish
   Kafka **`payment-processed`**.
4. On `StripeException` → save `Payment(status=FAILED, errorMessage=…)`, publish
   **`payment-failed`**, then rethrow `PaymentException`.
   `@Transactional(noRollbackFor = PaymentException.class)` keeps the FAILED audit row.

## Configuration

- `STRIPE_API_KEY` (env, from `deployment/docker/.env`) — a `sk_test_…` test key.
  Defaults to `sk_test_dummy`, which Stripe rejects → every charge fails until set.
- `StripeConfig` sets the global `Stripe.apiKey` at startup.
- Test tokens: `tok_visa` (success), `tok_chargeDeclined` (decline).

## Events

- **Produces:** `payment-processed`, `payment-failed`, `payment-refunded`
- **Consumes:** none (notification-service consumes the above for emails)

## Key files

`PaymentServiceImpl`, `PaymentController`, `config/StripeConfig`, `entity/Payment`,
`application.yaml` (`stripe.api.key`).
