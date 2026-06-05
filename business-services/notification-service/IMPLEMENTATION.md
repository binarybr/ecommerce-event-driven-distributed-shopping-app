# Notification Service — Implementation

**Status:** ✅ Complete & verified | **Port:** 8084 | **DB:** MySQL `notification_service`

## Purpose
Sends email/SMS/push notifications (email via SMTP/MailHog) and persists every
notification as an audit record. Reacts to platform events via Kafka.

## Components
| Layer | Class(es) |
|-------|-----------|
| Entity | `Notification` (recipient, type, status PENDING/SENT/FAILED, errorMessage) |
| Repository | `NotificationRepository` |
| DTO | `NotificationRequestDto` |
| Providers | `SmsProvider`/`MockSmsProvider`, `PushProvider`/`MockPushProvider` |
| Service | `NotificationService` / `NotificationServiceImpl` |
| Controller | `NotificationController` |
| Config | `KafkaConsumerConfig` (`@EnableKafka`), `KafkaRetryConfig` (`@EnableKafkaRetryTopic`) |
| Consumers | `OrderPlacedConsumer`, `UserRegisteredConsumer`, `PaymentEventConsumer` |

## Endpoints
| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/notifications/email` | Send email (202 Accepted) |
| POST | `/api/notifications/sms` | Send SMS (mock provider) |
| POST | `/api/notifications/push` | Send push (mock provider) |
| GET | `/api/notifications` | All notifications (audit) |
| GET | `/api/notifications/{id}` | One notification |

## Events consumed → emails sent
| Topic | Source | Email |
|-------|--------|-------|
| `user-registered` | user-service | Welcome |
| `order-placed` | order-service | Order confirmation |
| `payment-processed` | payment-service | Payment success |
| `payment-failed` | payment-service | Payment failure |

## Key design decisions
- **Audit-first:** every notification is persisted (PENDING) before send, then updated to SENT/FAILED — full history regardless of outcome.
- **Retry topics / DLT:** `KafkaRetryConfig` enables `@EnableKafkaRetryTopic`, the non-blocking retry→dead-letter mechanism that prevents a single bad message from blocking/looping the consumer.
- **Pluggable providers:** SMS/push are behind provider interfaces (currently mock implementations) ready for Twilio/FCM.

## Notes / follow-ups
- SMTP is wired to MailHog locally (port 1025; UI 8025); swap host/port for a real provider in production.
