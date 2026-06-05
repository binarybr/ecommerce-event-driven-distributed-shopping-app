# User Service — Implementation

**Status:** ✅ Complete & verified | **Port:** 8085 | **DB:** MySQL `user_service`

## Purpose
Identity provider for the platform: registration, login, JWT issuance, and
role-based access control (RBAC). This is the **only** service that *mints*
JWTs — every other service validates them with the shared HMAC secret.

## Components
| Layer | Class(es) |
|-------|-----------|
| Entity | `User` (email unique, BCrypt password, role, verification token) |
| Repository | `UserRepository` (findByEmail, existsByEmail, findByVerificationToken) |
| DTOs | `UserRegistrationDto`, `LoginRequestDto`, `AuthResponseDto`, `UserResponseDto` |
| Security | `JwtUtil` (mint+validate), `JwtAuthenticationFilter`, `SecurityConfig` |
| Service | `UserService` / `UserServiceImpl` |
| Controller | `UserController` |
| Mapper | `UserMapper` (omits password/token from responses) |
| Kafka | `KafkaConfig` (topic `user-registered`) |

## Endpoints
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/api/users/register` | public | Validates email/password(≥8)/phone(10-15 digits); BCrypt-hashes password |
| POST | `/api/users/login` | public | Returns JWT (subject=email, claims: userId, role) |
| GET | `/api/users` | ADMIN | List all users |
| GET | `/api/users/{id}` | CUSTOMER/ADMIN | |
| GET | `/api/users/email/{email}` | CUSTOMER/ADMIN | |
| PUT | `/api/users/{id}` | CUSTOMER/ADMIN | Profile fields only (no role/email/password) |
| DELETE | `/api/users/{id}` | ADMIN | |
| POST | `/api/users/verify-email` | public | |

## Events
- **Publishes** `user-registered` → consumed by notification-service (welcome email) and recommendation-service.

## Key design decisions
- **Stateless auth:** JWT carries userId + role so downstream services authorize locally without a callback.
- **Least privilege:** new users default to `CUSTOMER`; ADMIN promotion is a manual/DB action.
- **Password safety:** only the BCrypt hash is persisted; plaintext is never stored or logged.
- **Async welcome email:** registration publishes a Kafka event and returns immediately — it does not block on email delivery.

## Notes / follow-ups
- Login on an unknown email returns 404 (minor user-existence info leak) — acceptable for this app; could be normalized to a generic 401.
- JWT secret defaults to a dev value; set `JWT_SECRET` in production.
