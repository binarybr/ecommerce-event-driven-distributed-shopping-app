# api-gateway

**Port:** 8080 · Stack: Spring Cloud Gateway (reactive / WebFlux)

The single entry point for all external traffic (browser and the SPA's nginx proxy).
Routes `/api/**` to the right service by path, using Eureka for load-balanced discovery.

## Routes (`lb://` = resolved via Eureka)

| Path predicate | Target |
|---|---|
| `/api/users/**` | user-service |
| `/api/products/**` | product-service |
| `/api/orders/**` | order-service |
| `/api/inventory/**` | inventory-service |
| `/api/notifications/**` | notification-service |
| `/api/payments/**` | payment-service |
| `/api/cart/**` | cart-service |
| `/api/reviews/**` | review-service |
| `/api/recommendations/**` | recommendation-service |
| `/api/admin/**` | admin-service |

## Security (`SecurityConfig`, reactive)

- CSRF disabled (stateless API).
- `/actuator/health`, `/actuator/info` → public.
- **`/api/**` → `permitAll()`** at the gateway. JWT validation and role enforcement
  happen **downstream** in each service — the gateway just routes and forwards the
  `Authorization` header.
- Other actuator/management endpoints (`gateway/routes`, etc.) require HTTP Basic auth
  (`GATEWAY_USERNAME`/`GATEWAY_PASSWORD`, default `admin`/`admin`) for admin tooling.

## Why single-origin matters

In production the SPA is served by nginx, which proxies `/api/` to this gateway —
browser sees one origin, so there's **no CORS**. In dev, Vite's proxy does the same.

## Key files

`config/SecurityConfig`, `application.yaml` (routes).
