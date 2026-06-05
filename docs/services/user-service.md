# user-service

**Port:** 8085 · **DB:** MySQL `user_service` · **Auth:** issues JWTs

Owns accounts and authentication. The only service that **signs** JWTs; every other
service merely validates them with the shared `JWT_SECRET`.

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/users/register` | public | Create account (CUSTOMER, or ADMIN with key) |
| POST | `/api/users/login` | public | Authenticate, return JWT + user |
| GET | `/api/users` | ADMIN | List all users |
| GET | `/api/users/{id}` | ADMIN/CUSTOMER | Fetch one user (used by order-service via Feign) |
| GET | `/api/users/email/{email}` | ADMIN/CUSTOMER | Fetch by email |
| PUT | `/api/users/{id}` | ADMIN/CUSTOMER | Update profile |
| DELETE | `/api/users/{id}` | ADMIN | Delete user |
| POST | `/api/users/verify-email?token=` | public | Mark email verified |

## Registration flow

1. Reject duplicate email up-front (also a unique DB column).
2. **Role resolution** (`resolveRole`): if `adminKey` is blank → `CUSTOMER`; if it
   matches env `ADMIN_REGISTRATION_KEY` → `ADMIN`; otherwise → 400. No self-service
   privilege escalation.
3. BCrypt-hash the password (plaintext never stored/logged).
4. Save user (`enabled=true`, `emailVerified=false`, 24h verification token).
5. Publish Kafka **`user-registered`** → notification-service sends a welcome email
   (registration does **not** block on email delivery).

## Login flow

1. Look up by email → `UserNotFoundException` if absent.
2. Reject if `enabled=false`.
3. `BCrypt.matches` (constant-time) — never compares plaintext.
4. Update `lastLoginAt`.
5. `jwtUtil.generateToken(email, userId, role)` → token carries `userId` + `role`
   claims so downstream services authorize without calling back.

## Admin seeding

`DataSeeder` (ApplicationRunner) creates a default admin on first boot if absent.
Credentials come from env (`ADMIN_EMAIL`/`ADMIN_PASSWORD`/…), default
`admin@shopsphere.com` / `Admin@1234`. Idempotent — never resets a changed password.

## Events

- **Produces:** `user-registered`
- **Consumes:** none

## Key files

`UserServiceImpl` (logic), `UserController`, `JwtUtil` (HS signing),
`JwtAuthenticationFilter`, `SecurityConfig`, `config/DataSeeder`, `UserRegistrationDto`.
