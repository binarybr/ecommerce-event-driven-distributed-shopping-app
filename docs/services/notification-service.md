# notification-service

**Port:** 8084 · **DB:** MySQL `notification_service` · **SMTP:** MailHog (1025, UI 8025)

Event-driven. It does not expose business write endpoints — it **reacts to Kafka
events** from other services and sends emails, persisting each as a `notification`
row (recipient, subject, message, type, status, timestamps).

## Consumers (Kafka)

| Listener | Topic | Action |
|---|---|---|
| `UserRegisteredConsumer` | `user-registered` | Welcome email to the new user |
| `OrderPlacedConsumer` | `order-placed` | Order confirmation email |
| `PaymentEventConsumer` | `payment-processed` | Payment success receipt |
| `PaymentEventConsumer` | `payment-failed` | Payment failure notice |

All listeners share `groupId = "notification-group"`.

## Email delivery

Sent via SMTP to **MailHog** (a fake SMTP server for development). No real email
leaves the machine — open **http://localhost:8025** to view every message. Each send
is recorded with `status = SENT` / `FAILED` (+ `errorMessage` on failure) for audit.

## Read access

Notification history is surfaced to admins through **admin-service**
(`GET /api/admin/notifications`, `…/notifications/failed`), which reads from this
service's data.

## Events

- **Produces:** none
- **Consumes:** `user-registered`, `order-placed`, `payment-processed`, `payment-failed`

## Key files

`consumer/UserRegisteredConsumer`, `consumer/OrderPlacedConsumer`,
`consumer/PaymentEventConsumer`, `config/KafkaConsumerConfig`, `entity/Notification`.
