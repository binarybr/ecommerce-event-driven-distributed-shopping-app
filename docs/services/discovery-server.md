# discovery-server

**Port:** 8761 · Stack: Netflix Eureka Server

The service registry. Every other service registers itself here on startup and uses it
to locate peers for Feign / load-balanced (`lb://`) calls.

## Role in the system

- **Registration:** each service (with `eureka.client` enabled) registers under its
  application name (e.g. `ORDER-SERVICE`) with its host/port and health status.
- **Discovery:** Feign clients use `@FeignClient(name = "inventory-service")` and the
  gateway uses `uri: lb://inventory-service` — both resolve the real address through
  Eureka instead of hard-coded hosts. This is what makes the services relocatable and
  load-balanceable.
- **Dashboard:** http://localhost:8761 shows all registered instances and their status
  (handy for debugging "503 / no instance available" — it means the target hasn't
  registered yet).

## Startup ordering

Most services `depends_on` discovery-server. After a fresh `up`, allow ~60–90s for all
services to register before expecting `/api/**` calls to succeed (a call to an
unregistered service returns 503 at the gateway).

## Key files

`application.yaml` (Eureka server config), main application class with
`@EnableEurekaServer`.
