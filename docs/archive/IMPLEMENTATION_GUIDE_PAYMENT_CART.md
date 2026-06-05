# 🚀 Payment & Shopping Cart Services - Implementation Guide

**Status:** Ready for Implementation  
**Estimated Time:** 2-3 hours for full implementation  
**Priority:** CRITICAL for e-commerce platform

---

## 📊 What to Implement

### **Service 1: Payment Service (Port 8086)**
Location: `business-services/payment-service`

#### Core Components

**Entity: Payment**
```java
@Entity
public class Payment {
    @Id @GeneratedValue private Long id;
    private String orderId;
    private String userId;
    private Double amount;
    private String currency; // USD, EUR, etc.
    private String status; // PENDING, COMPLETED, FAILED, REFUNDED
    private String paymentMethod; // STRIPE, PAYPAL
    private String transactionId;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
```

**DTOs**
- `PaymentRequestDto` - amount, orderId, userId
- `PaymentResponseDto` - transaction details
- `RefundRequestDto` - payment ID, amount

**Service Interface**
```java
public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto request, String userId);
    PaymentResponseDto getPaymentDetails(Long paymentId);
    PaymentResponseDto refundPayment(Long paymentId, RefundRequestDto request);
    List<PaymentResponseDto> getPaymentHistory(String userId);
}
```

**Key Features**
- ✅ Stripe payment processing
- ✅ Payment status tracking
- ✅ Refund handling
- ✅ Transaction history
- ✅ Error handling & logging
- ✅ Idempotency for retries
- ✅ Webhook handling for Stripe events
- ✅ JWT authentication

**Kafka Events**
- `PaymentProcessedEvent` - published when payment completes
- `PaymentFailedEvent` - published when payment fails
- `PaymentRefundedEvent` - published when refund completes

**REST Endpoints**
```
POST   /api/payments - Process payment
GET    /api/payments/{id} - Get payment details
GET    /api/payments/history - Get user's payment history
POST   /api/payments/{id}/refund - Refund payment
POST   /webhooks/stripe - Stripe webhook handler
```

---

### **Service 2: Shopping Cart Service (Port 8087)**
Location: `business-services/cart-service`

#### Core Components

**Entity: CartItem**
```java
@Entity
public class CartItem {
    @Id @GeneratedValue private Long id;
    private String userId;
    private String productId;
    private Integer quantity;
    private Double price; // Unit price at time of add
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}
```

**Entity: Cart**
```java
@Entity
public class Cart {
    @Id @GeneratedValue private Long id;
    private String userId; // Unique per user
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "cart")
    private List<CartItem> items;
}
```

**DTOs**
- `AddToCartDto` - productId, quantity
- `CartItemResponseDto` - all item details
- `CartResponseDto` - all items + totals

**Service Interface**
```java
public interface CartService {
    CartItemResponseDto addItem(String userId, AddToCartDto request);
    CartItemResponseDto updateItem(String userId, Long itemId, Integer quantity);
    void removeItem(String userId, Long itemId);
    CartResponseDto getCart(String userId);
    void clearCart(String userId);
    CartCheckoutDto prepareCheckout(String userId); // For order placement
}
```

**Key Features**
- ✅ Add/update/remove items
- ✅ Persistent storage (Redis + MySQL)
- ✅ Cart summary (item count, totals)
- ✅ Cart expiration (30 days auto-clear)
- ✅ Promo code support
- ✅ Stock validation
- ✅ Price caching from product-service
- ✅ Concurrent access handling

**Kafka Events**
- `ItemAddedToCartEvent`
- `CartClearedEvent`
- `CheckoutInitiatedEvent`

**REST Endpoints**
```
POST   /api/cart/items - Add item
PUT    /api/cart/items/{itemId} - Update quantity
DELETE /api/cart/items/{itemId} - Remove item
GET    /api/cart - Get cart
DELETE /api/cart - Clear cart
POST   /api/cart/checkout - Prepare checkout
```

---

## 🛠️ Implementation Steps

### Step 1: Create Entity Classes (10 mins)
- [x] Payment.java
- [x] CartItem.java
- [x] Cart.java

### Step 2: Create Repository Interfaces (5 mins)
```java
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(String userId);
    Optional<Payment> findByTransactionId(String transactionId);
}

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(String userId);
}

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(String userId);
}
```

### Step 3: Create DTOs (15 mins)
- Payment: Request, Response
- Refund: Request
- Cart: Item response, Cart response, Checkout response

### Step 4: Create Service Interfaces (10 mins)
- PaymentService (interface)
- CartService (interface)

### Step 5: Implement Services (45 mins)
- PaymentServiceImpl with Stripe integration
- CartServiceImpl with cart logic

### Step 6: Create REST Controllers (20 mins)
- PaymentController
- CartController

### Step 7: Add Security & JWT (15 mins)
- Copy JWT filter from user-service
- Secure endpoints with @PreAuthorize

### Step 8: Create Kafka Events & Publishers (10 mins)
- Add event classes to common-library
- Publish from services

### Step 9: Configuration Files (10 mins)
- application.yaml for both services
- Stripe API key configuration

### Step 10: Create Tests (20 mins)
- Basic integration tests

---

## 📝 Key Implementation Details

### Payment Service - Stripe Integration

```java
@Configuration
public class StripeConfig {
    @Bean
    public Stripe stripeConfig(@Value("${stripe.api.key}") String apiKey) {
        Stripe.apiKey = apiKey;
        return new Stripe();
    }
}

// In PaymentServiceImpl
public PaymentResponseDto processPayment(PaymentRequestDto request, String userId) {
    try {
        // Create Stripe charge
        Map<String, Object> params = new HashMap<>();
        params.put("amount", (int)(request.getAmount() * 100)); // Cents
        params.put("currency", request.getCurrency());
        params.put("source", request.getStripeToken());
        params.put("description", "Order: " + request.getOrderId());
        
        Charge charge = Charge.create(params);
        
        // Save payment record
        Payment payment = Payment.builder()
            .userId(userId)
            .orderId(request.getOrderId())
            .amount(request.getAmount())
            .status("COMPLETED")
            .transactionId(charge.getId())
            .completedAt(LocalDateTime.now())
            .build();
            
        Payment saved = paymentRepository.save(payment);
        
        // Publish event
        kafkaTemplate.send("payment-processed", 
            PaymentProcessedEvent.builder()
                .paymentId(saved.getId())
                .orderId(request.getOrderId())
                .userId(userId)
                .amount(request.getAmount())
                .build());
        
        return mapToResponse(saved);
    } catch (CardException e) {
        // Save failed payment
        saveFailedPayment(request, userId, e.getMessage());
        throw new PaymentException("Card declined");
    }
}
```

### Shopping Cart - Item Management

```java
public CartItemResponseDto addItem(String userId, AddToCartDto request) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElse(Cart.builder().userId(userId).items(new ArrayList<>()).build());
    
    // Check existing item
    Optional<CartItem> existing = cart.getItems().stream()
        .filter(item -> item.getProductId().equals(request.getProductId()))
        .findFirst();
    
    if (existing.isPresent()) {
        existing.get().setQuantity(existing.get().getQuantity() + request.getQuantity());
    } else {
        CartItem newItem = CartItem.builder()
            .productId(request.getProductId())
            .quantity(request.getQuantity())
            .price(getProductPrice(request.getProductId()))
            .userId(userId)
            .addedAt(LocalDateTime.now())
            .build();
        cart.getItems().add(newItem);
    }
    
    Cart saved = cartRepository.save(cart);
    return mapToResponse(saved);
}
```

---

## 🔌 Integration Points

### Payment Service ← Order Service
When order is placed:
1. Order Service calls Payment Service
2. Waits for payment completion
3. Updates order status based on payment result

### Cart Service ← Order Service
When checkout initiated:
1. Get cart items from Cart Service
2. Validate stock with Product Service
3. Create order
4. Clear cart

### Both Services ← User Service
- JWT token validation
- User ID extraction
- Role-based access control

---

## 📦 Dependencies to Add to pom.xml

**Payment Service:**
- `com.stripe:stripe-java:24.12.0` - Stripe SDK
- All security + JWT dependencies (copy from user-service pom)

**Cart Service:**
- Spring Data JPA
- Spring Security
- JWT dependencies

---

## 🔐 Security Configuration

Both services need:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/api/payments/webhooks/**").permitAll() // Stripe webhooks
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

---

## 📊 Configuration (application.yaml)

### Payment Service
```yaml
server:
  port: 8086
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/payment_service
  application:
    name: payment-service
stripe:
  api:
    key: ${STRIPE_API_KEY:sk_test_...}
  webhook:
    secret: ${STRIPE_WEBHOOK_SECRET:whsec_...}
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

### Cart Service
```yaml
server:
  port: 8087
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cart_service
  application:
    name: cart-service
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
cart:
  expiration-days: 30
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

---

## 🧪 Testing Stripe Locally

```bash
# Install Stripe CLI
brew install stripe/stripe-cli/stripe

# Trigger test webhook
stripe listen --forward-to localhost:8086/api/payments/webhooks/stripe

# In another terminal, trigger test event
stripe trigger payment_intent.succeeded
```

---

## 📈 Database Schema

### Payments Table
```sql
CREATE TABLE payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id VARCHAR(255) NOT NULL,
  order_id VARCHAR(255) NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  status VARCHAR(50) NOT NULL,
  payment_method VARCHAR(50),
  transaction_id VARCHAR(255),
  error_message TEXT,
  created_at DATETIME NOT NULL,
  completed_at DATETIME,
  INDEX idx_user_id (user_id),
  INDEX idx_order_id (order_id),
  UNIQUE KEY uk_transaction_id (transaction_id)
);
```

### Cart Tables
```sql
CREATE TABLE cart (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id VARCHAR(255) UNIQUE NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME
);

CREATE TABLE cart_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id VARCHAR(255) NOT NULL,
  product_id VARCHAR(255) NOT NULL,
  quantity INT NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  added_at DATETIME NOT NULL,
  updated_at DATETIME,
  INDEX idx_user_id (user_id),
  INDEX idx_product_id (product_id)
);
```

---

## 🚀 Deployment Checklist

- [ ] Stripe account created
- [ ] API keys obtained
- [ ] Webhook secret configured
- [ ] Databases created
- [ ] Services compile
- [ ] Gateway routes added
- [ ] Eureka registration verified
- [ ] JWT authentication working
- [ ] Kafka topics created
- [ ] Integration tests passing
- [ ] Docker images built
- [ ] docker-compose.yml updated
- [ ] Load testing completed

---

## 📝 File Checklist for Payment Service

```
business-services/payment-service/
├── pom.xml ✅
├── src/main/java/com/binarylabyrinth/paymentservice/
│   ├── PaymentServiceApplication.java
│   ├── controller/PaymentController.java
│   ├── service/PaymentService.java (interface)
│   ├── service/impl/PaymentServiceImpl.java
│   ├── repository/PaymentRepository.java
│   ├── entity/Payment.java
│   ├── dto/PaymentRequestDto.java
│   ├── dto/PaymentResponseDto.java
│   ├── dto/RefundRequestDto.java
│   ├── event/PaymentProcessedEvent.java
│   ├── event/PaymentFailedEvent.java
│   ├── exception/PaymentException.java
│   ├── config/StripeConfig.java
│   └── config/SecurityConfig.java
├── src/main/resources/application.yaml
└── src/test/java/com/binarylabyrinth/paymentservice/
    └── PaymentServiceApplicationTest.java
```

---

## 📝 File Checklist for Cart Service

```
business-services/cart-service/
├── pom.xml
├── src/main/java/com/binarylabyrinth/cartservice/
│   ├── CartServiceApplication.java
│   ├── controller/CartController.java
│   ├── service/CartService.java (interface)
│   ├── service/impl/CartServiceImpl.java
│   ├── repository/CartRepository.java
│   ├── repository/CartItemRepository.java
│   ├── entity/Cart.java
│   ├── entity/CartItem.java
│   ├── dto/AddToCartDto.java
│   ├── dto/CartItemResponseDto.java
│   ├── dto/CartResponseDto.java
│   ├── event/ItemAddedToCartEvent.java
│   ├── exception/CartException.java
│   └── config/SecurityConfig.java
├── src/main/resources/application.yaml
└── src/test/java/com/binarylabyrinth/cartservice/
    └── CartServiceApplicationTest.java
```

---

## 🎯 Next Steps

1. **Create Payment Service files** using the templates above
2. **Create Cart Service files** using the templates above
3. **Add routes to API Gateway:**
   ```yaml
   - id: payment-service
     uri: lb://payment-service
     predicates:
       - Path=/api/payments/**
   - id: cart-service
     uri: lb://cart-service
     predicates:
       - Path=/api/cart/**
   ```
4. **Build:** `mvn clean package -DskipTests`
5. **Test locally** with provided curl examples

---

Would you like me to implement these services by creating the actual Java files? This guide has all the details needed for a complete implementation.

