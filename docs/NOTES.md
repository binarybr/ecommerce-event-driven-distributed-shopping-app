# ShopSphere — Technical Notes & Cheat-Sheet

Reference notes on every component and feature in the stack: **what it is**, **why it's
used here**, and a **short example** from (or representative of) this codebase.

> Companion docs: [`STORY.md`](./STORY.md) (the why/journey), [`WORKFLOW.md`](./WORKFLOW.md)
> (the map), [`services/`](./services) (per-service detail).

---

## A. Architecture Concepts

### Microservices
**What:** the app is split into many small services, each owning one capability and its
own database.
**Why here:** independent deploys, isolated failures, polyglot storage, team autonomy.
**Example:** `order-service`, `payment-service`, `inventory-service` are separate Spring
Boot apps with separate MySQL schemas.

### Polyglot persistence
**What:** use the right database per service.
**Why here:** products are document-shaped + searchable (MongoDB); orders/payments are
transactional (MySQL).
**Example:** `product-service → MongoDB product_service`, `order-service → MySQL order_service`.

### Synchronous vs Asynchronous communication
- **Synchronous (Feign):** caller needs an answer now. *Order → Inventory: "reserve this."*
- **Asynchronous (Kafka):** fire-and-forget. *Order placed → emit `order-placed` → email + inventory react.*

### Eventual consistency
**What:** some state converges over time, not instantly.
**Example:** order is saved `PLACED`, then a Kafka `inventory-reserved` event flips it to
`CONFIRMED` a moment later (`InventoryEventConsumer`).

---

## B. Spring Boot Core

### Spring Boot
**What:** opinionated auto-configuration over Spring.
**Example:** a service is just a `@SpringBootApplication` class + `application.yaml`.
```java
@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) { SpringApplication.run(OrderServiceApplication.class, args); }
}
```

### Spring Web (MVC) — REST controllers
**What:** servlet-based REST endpoints (embedded Tomcat).
**Example:**
```java
@RestController
@RequestMapping("/api/orders")
class OrderController {
    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(@Valid @RequestBody OrderRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(dto));
    }
}
```

### Bean Validation (`@Valid`, JSR-380)
**What:** declarative input validation; failures → 400 via the exception handler.
**Example:**
```java
@NotBlank(message = "Product ID is required") private String productId;
@Min(value = 1, message = "Quantity must be at least 1") private Integer quantity;
```

### `@RestControllerAdvice` — global exception handling
**What:** centralizes exception → HTTP status mapping.
**Example:**
```java
@ExceptionHandler(ProductOutOfStockException.class)
ResponseEntity<ErrorResponse> handle(ProductOutOfStockException ex) {
    return ResponseEntity.badRequest().body(/* 400 ErrorResponse */);
}
```

### Spring Boot Actuator
**What:** ops endpoints (`/actuator/health`, `/metrics`, `/prometheus`).
**Why here:** health checks for Docker/compose and Eureka, Prometheus scraping.

---

## C. Spring Cloud (Distributed Systems)

### Eureka (Service Discovery)
**What:** a registry where services register and look each other up by name.
**Why here:** Feign and the gateway address services as `lb://order-service`, not by IP.
**Example (client):**
```yaml
eureka.client.service-url.defaultZone: http://discovery-server:8761/eureka
```
> Debug tip: a `503` at the gateway usually means the target hasn't registered yet.

### Spring Cloud Gateway (reactive)
**What:** the single entry point; routes `/api/**` to services.
**Example:**
```yaml
routes:
  - id: order-service
    uri: lb://order-service
    predicates: [ Path=/api/orders/** ]
```

### Spring Cloud Config
**What:** centralized configuration server (8888); services can fetch config centrally.
**Why here:** one place for shared settings; local `application.yaml` + env vars override.

### OpenFeign — declarative HTTP client
**What:** define an interface, Feign generates the HTTP client; load-balanced via Eureka.
**Example:**
```java
@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable String id);
}
```
**Auth forwarding** (so downstream `@PreAuthorize` passes):
```java
@Bean RequestInterceptor authForwarder() {
    return tpl -> tpl.header("Authorization",
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest().getHeader("Authorization"));
}
```

---

## D. Data Access

### Spring Data JPA (MySQL)
**What:** repository interfaces over Hibernate; methods derived from names.
**Example:**
```java
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(String productId);
}
```
**`ddl-auto: update`** auto-creates/updates tables from entities at startup.

### JPA Optimistic Locking (`@Version`)
**What:** detects concurrent updates; prevents lost updates without DB locks.
**Why here:** two customers can't both reserve the last unit.
**Example:**
```java
@Entity class Inventory {
    @Version private Integer version;   // Hibernate bumps & checks this on save
}
```

### Spring Data MongoDB
**What:** document mapping + repositories for MongoDB.
**Example (text-indexed catalog):**
```java
@Document(collection = "products")
class Product {
    @Id String id;
    @TextIndexed(weight = 5) String name;     // strongest search signal
    @TextIndexed(weight = 1) String description;
    @Indexed String category;                 // exact-match filter
}
```

### Transactions (`@Transactional`)
**What:** unit-of-work boundaries.
**Example (subtle):** payment uses `noRollbackFor` so a FAILED audit row survives the
rethrow:
```java
@Transactional(noRollbackFor = PaymentException.class)
public class PaymentServiceImpl { /* save FAILED record, then throw */ }
```

---

## E. Security

### JWT (stateless auth)
**What:** signed token carrying identity + role; no server session.
**Why here:** any service validates locally with the shared secret — no auth callbacks.
**Token payload example:** `{"sub":"user@x.com","userId":1,"role":"ADMIN","exp":…}`

### Signing & validation (jjwt)
```java
// user-service signs:
Jwts.builder().subject(email).claim("role", role).signWith(key).compact();
// every service validates:
Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
```

### `JwtAuthenticationFilter` (per service)
**What:** reads `Authorization: Bearer …`, validates, sets the security context.
```java
var authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + role));
var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
SecurityContextHolder.getContext().setAuthentication(auth);
```

### Authorization (RBAC)
```java
.requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")   // URL-based
.requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()       // public reads
```
```java
@PreAuthorize("hasRole('ADMIN')")   // method-based (admin-service)
```

### BCrypt password hashing
**What:** salted, slow one-way hash.
```java
passwordEncoder.encode(rawPassword);              // on register
passwordEncoder.matches(candidate, storedHash);   // on login (constant-time)
```

---

## F. Messaging — Apache Kafka

### Producer
```java
kafkaTemplate.send("order-placed", OrderPlacedEvent.builder()
    .orderNumber(order.getOrderNumber()).productId(...).quantity(...).build());
```

### Consumer
```java
@KafkaListener(topics = "inventory-reserved", groupId = "order-group")
public void onReserved(InventoryReservedEvent e) {
    // PLACED -> CONFIRMED
}
```
**Shared events** live in the `common-library` module (`OrderPlacedEvent`,
`ProductCreatedEvent`, …) and are JSON-serialized (`spring.json.trusted.packages`).

**Consumer groups:** `groupId` controls delivery — one member of a group gets each
message (e.g. all notification listeners share `notification-group`).

---

## G. Resilience — Resilience4j Circuit Breaker

**What:** stops calling a failing dependency; "opens" after a failure threshold, then
half-opens to test recovery.
**Example:**
```java
@CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
public OrderResponseDto placeOrder(OrderRequestDto dto) { /* Feign call to inventory */ }
```
```yaml
resilience4j.circuitbreaker.instances.inventory:
  sliding-window-size: 10
  failure-rate-threshold: 50
  wait-duration-in-open-state: 10s
  ignore-exceptions:           # business outcomes must NOT trip the breaker
    - com.binarylabyrinth.orderservice.exception.ProductOutOfStockException
```
**Lesson baked in:** without `ignore-exceptions`, "out of stock" counted as failure and
opened the breaker, rejecting all orders.

---

## H. Payments — Stripe

**What:** card processing SDK.
**Key detail:** idempotency key goes in the **header**, not the body.
```java
RequestOptions opts = RequestOptions.builder()
    .setIdempotencyKey(userId + "-" + orderId).build();   // prevents double-charge on retry
Charge charge = Charge.create(params, opts);              // params: amount(cents), currency, source token
```
- Key from gitignored `.env` (`STRIPE_API_KEY=sk_test_…`).
- Test tokens: `tok_visa` (success), `tok_chargeDeclined` (decline).

---

## I. Productivity Libraries

### Lombok
**What:** generates boilerplate at compile time.
```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor   // getters/setters/builder/ctors
@Slf4j                                                   // private static final Logger log
```

### Jackson
**What:** JSON (de)serialization for REST + Kafka. `@JsonProperty`, `@JsonIgnoreProperties(ignoreUnknown=true)`
keeps DTOs tolerant of extra fields (used in order-service `ProductDto`).

---

## J. Infrastructure & Ops

### Docker & Docker Compose
**What:** containerized services + infra defined as one stack.
**Key build note:** service `Dockerfile`s `COPY target/*.jar` — they **don't compile**.
Always `mvnw clean package` first.
```dockerfile
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

### Named volumes (persistence)
**Why here:** without them, recreating a DB container wipes all data (a bug we hit).
```yaml
volumes: [ mysql-data:/var/lib/mysql ]   # survives down/up & rebuilds
```

### Port remapping (avoid host conflicts)
```yaml
mysql:  ports: ["3307:3306"]    # host 3307 -> container 3306
mongo:  ports: ["27018:27017"]  # avoid clashing with a local MongoDB on 27017
```

### MailHog
**What:** fake SMTP server for dev; capture emails at http://localhost:8025.

### Maven multi-module + wrapper
**What:** parent `pom.xml` builds `common-library` + all services in order; `mvnw`
pins the Maven version.

---

## K. Frontend (brief)

- **React + TypeScript + Vite** SPA; **React Router** for pages.
- **Axios interceptors:** attach `Authorization: Bearer` on every request; on 401 clear
  the token.
- **Context API:** `AuthContext`, `CartContext`, `ToastContext` for global state.
- **Single origin in prod:** multi-stage Docker (Node build → **nginx**) where nginx
  proxies `/api/` to the gateway → no CORS.

---

## L. Java Language Features Used (with examples)

| Feature | Since | Example in project |
|---|---|---|
| Diamond operator | 7 | `Map<String,Object> p = new HashMap<>();` |
| Lambdas / method refs | 8 | `.map(mapper::toResponseDto)` |
| Streams | 8 | `items.stream().mapToDouble(i -> i.price()*i.qty()).sum()` |
| `Optional` | 8 | `repo.findByProductId(id).orElseThrow(...)` |
| `java.time` | 8 | `LocalDateTime.now()` on entities/events |
| `Optional.ifPresentOrElse` | 9 | inventory upsert (update-or-create) |
| `List.of(...)` | 9 | demo product seed list |
| `var` | 10 | `var auth = new UsernamePasswordAuthenticationToken(...)` |
| `Stream.toList()` | 16 | `users.stream().map(...).toList()` |

**Modernization opportunities** (available on Java 25, not yet adopted):
```java
// pattern matching for instanceof (16):
if (ex instanceof ProductOutOfStockException poe) throw poe;
// records for DTOs (16):
record ProductDto(String id, String name, Double price) {}
// switch expression (17/21):
String css = switch (status) { case "CONFIRMED" -> "green"; default -> "amber"; };
// text blocks (15) for SQL/JSON literals
```

---

## M. One-line glossary

- **Feign** = declarative HTTP client (interface → calls).
- **Eureka** = phone book for services.
- **Gateway** = front door; routes by URL.
- **Circuit breaker** = stop calling a sick dependency.
- **Idempotency key** = "process this once, even if I retry."
- **Optimistic locking** = detect concurrent edits via a version column.
- **Eventual consistency** = correct soon, not instantly.
- **Polyglot persistence** = right database per job.
