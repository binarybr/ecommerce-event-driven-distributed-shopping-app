# Project File Structure & Purposes

## Overview
This document provides a comprehensive guide to all files in the online-shopping-app microservices project.

---

## Root Level Files

### Configuration & Build Files
```
pom.xml                              - Parent Maven POM with all module definitions
mvnw, mvnw.cmd                       - Maven wrapper for consistent builds
```

### Documentation
```
README_ANALYSIS.md                   - Initial analysis indexing document
PROJECT_SUMMARY.md                   - Executive summary with completion status
MISSING_IMPLEMENTATIONS_ANALYSIS.md  - Detailed service-by-service analysis
BUGS_AND_ISSUES.md                   - Comprehensive bug report with fixes
ARCHITECTURE_DIAGRAM.md              - Visual architecture and data flows
QUICK_REFERENCE.md                   - Quick lookup checklist format
COMPLETION_SUMMARY.md                - Total project completion status (NEW)
TESTING_DEPLOYMENT_GUIDE.md          - Testing and deployment procedures (NEW)
```

---

## Common Library (Shared Components)

### Maven Configuration
```
common-library/pom.xml               - Common library module dependencies
```

### Shared Event Classes
```
common-library/src/main/java/com/binarylabyrinth/message/
├── OrderPlacedEvent.java            - Event when order is placed
├── InventoryReservedEvent.java      - Event when inventory is reserved
├── InventoryFailedEvent.java        - Event when inventory check fails
└── ProductCreatedEvent.java         - Event when product is created
```

### Original Files (Kept for Reference)
```
common-library/src/main/java/com/binarylabyrinth/
└── App.java                         - Original hello world app
```

---

## Product Service (MongoDB-based)

### Configuration
```
business-services/product-service/pom.xml               - Service dependencies
business-services/product-service/src/main/resources/
└── application.yaml                 - Configuration (MongoDB, Kafka, Eureka)
```

### Application Main Class
```
business-services/product-service/src/main/java/
└── com/binarylabyrinth/productservice/
    └── ProductServiceApplication.java - Spring Boot application entry point
```

### REST Controller
```
ProductController.java                - REST endpoints for products
```

### Service Layer
```
service/
├── ProductService.java              - Interface defining business operations
└── impl/
    └── ProductServiceImpl.java       - Service implementation with:
                                        - Caching logic
                                        - Kafka event publishing
                                        - Business rules
```

### Data Layer
```
entity/
├── Product.java                     - MongoDB document entity
repository/
└── ProductRepository.java           - MongoDB repository interface
```

### Data Transfer Objects
```
dto/
├── ProductRequestDto.java           - API request payload
└── ProductResponseDto.java          - API response payload
```

### Utilities
```
mapper/
├── ProductMapper.java               - DTO ↔ Entity conversion
event/
├── ProductCreatedEvent.java         - Local event class (legacy)
exception/
├── ProductNotFoundException.java    - Custom exception
├── ErrorResponse.java              - Standard error response
└── GlobalExceptionHandler.java     - Central exception handling

config/
├── ...                             - Service configuration beans
security/
├── ...                             - Security configuration (if any)
handler/
├── ...                             - Event handlers (if any)
kafka/
├── ...                             - Kafka configuration (if any)
util/
├── ...                             - Utility classes (if any)
```

---

## Order Service (MySQL-based with Feign + Circuit Breaker)

### Configuration
```
business-services/order-service/pom.xml  - Service dependencies
business-services/order-service/src/main/resources/
└── application.yaml                 - Configuration (MySQL, Kafka, Resilience4j)
```

### Application Main Class
```
OrderServiceApplication.java         - Spring Boot app with @EnableFeignClients
```

### REST Controller
```
controller/
└── OrderController.java             - REST endpoints for orders
```

### Service Layer
```
service/
├── OrderService.java                - Interface
└── impl/
    └── OrderServiceImpl.java         - Implementation with:
                                        - Inventory validation via Feign
                                        - Circuit breaker pattern
                                        - Event publishing
```

### External Service Client
```
client/
└── InventoryClient.java             - Feign client for inventory service
                                        with load balancing (lb://)
```

### Data Layer
```
entity/
├── Order.java                       - JPA entity for MySQL
repository/
└── OrderRepository.java             - JPA repository interface
```

### Data Transfer Objects
```
dto/
├── OrderRequestDto.java             - FIXED: Now uses productId (not productName)
├── OrderResponseDto.java            - FIXED: Now uses productId (not productName)
└── InventoryResponseDto.java        - Response from inventory service
```

### Exception Handling
```
exception/
├── OrderNotFoundException.java      - Order not found exception
├── ProductOutOfStockException.java - Stock check failure
handler/
└── GlobalExceptionHandler.java     - Central exception handling
```

### Utilities
```
mapper/
├── OrderMapper.java                 - FIXED: Correct field mapping (productId)
event/
└── ...                             - Event definitions
```

---

## Inventory Service (MySQL-based with Kafka)

### Configuration
```
business-services/inventory-service/pom.xml     - Service dependencies
business-services/inventory-service/src/main/resources/
├── application.yaml                 - Main configuration
└── bootstrap.yml                    - Config server integration
```

### Application Main Class
```
InventoryServiceApplication.java     - Spring Boot application entry point
```

### REST Controller
```
controller/
└── InventoryController.java         - FIXED: Now proper REST controller
                                        GET /api/inventory
                                        POST /api/inventory
```

### Service Layer
```
service/
├── InventoryService.java            - Interface
└── impl/
    └── InventoryServiceImpl.java     - Implementation
```

### Kafka Consumer
```
consumer/
└── OrderPlacedConsumer.java         - FIXED: Consumes order-placed events
                                        Publishes inventory-reserved/failed events
                                        Uses shared event classes from common-library
```

### Data Layer
```
entity/
├── Inventory.java                   - FIXED: Now includes Lombok annotations
repository/
└── InventoryRepository.java         - Includes Custom query for findByProductId()
```

### Data Transfer Objects
```
dto/
├── InventoryRequestDto.java         - Add inventory request
└── InventoryResponseDto.java        - Stock check response
```

### Event Classes
```
event/
├── InventoryReservedEvent.java      - UPDATED: Enhanced with quantity, timestamp
└── InventoryFailedEvent.java        - UPDATED: Enhanced with productId, timestamp
```

### Exception Handling
```
exception/
├── InventoryNotFoundException.java  - Inventory not found
handler/
└── GlobalExceptionHandler.java     - Central exception handling
```

### Utilities
```
mapper/
├── InventoryMapper.java             - DTO ↔ Entity conversion
config/
├── ...                             - Configuration beans
```

---

## Notification Service (MySQL-based with Email/SMS/Push)

### Configuration
```
business-services/notification-service/pom.xml  - Dependencies with JPA + MySQL
business-services/notification-service/src/main/resources/
└── application.yaml                 - Configuration (MySQL, Kafka, Mail, Eureka)
```

### Application Main Class
```
NotificationServiceApplication.java  - Spring Boot application entry point
```

### REST Endpoints (If Needed)
```
controller/
└── ...                             - May add notification endpoints
```

### Service Layer
```
service/
├── NotificationService.java         - Interface with:
                                        sendEmail()
                                        sendSms()
                                        sendPushNotification()
└── impl/
    └── NotificationServiceImpl.java  - ENHANCED IMPLEMENTATION:
                                        - Email persistence + sending
                                        - SMS/Push logging to database
                                        - Transaction management
```

### Kafka Consumer
```
consumer/
└── OrderPlacedConsumer.java         - UPDATED: Uses shared OrderPlacedEvent
                                        Receives order-placed events
                                        Triggers email notification
```

### Data Layer (NEW)
```
entity/
├── Notification.java                - CREATED: JPA entity for notification history
                                        Fields: recipient, subject, message
                                        Status tracking: PENDING, SENT, FAILED
                                        Timestamps: createdAt, sentAt
repository/
└── NotificationRepository.java      - CREATED: JPA repository
                                        Methods: findByStatus()
                                        findByCreatedAtAfter()
                                        findByRecipient()
```

### Data Transfer Objects
```
dto/
└── NotificationRequestDto.java      - Request payload with recipient, subject, message
```

### Exception Handling
```
exception/
├── NotificationException.java       - Notification sending error
handler/
└── GlobalExceptionHandler.java     - Central exception handling
```

### Configuration
```
config/
├── ...                             - Mail configuration beans
```

---

## API Gateway (Spring Cloud Gateway)

### Configuration
```
infrastructure-services/api-gateway/pom.xml         - Gateway dependencies
infrastructure-services/api-gateway/src/main/resources/
└── application.yaml                 - CONFIGURED: Routes all requests to backend services
                                        Routes configured:
                                        /api/products/** → product-service
                                        /api/orders/** → order-service
                                        /api/inventory/** → inventory-service
                                        /api/notifications/** → notification-service
```

### Application Main Class
```
ApiGatewayApplication.java           - Spring Boot API Gateway application
```

### Gateway Features
- Load balancing via Eureka (lb://)
- Request routing with predicates
- Path prefix stripping
- Service discovery integration

---

## Discovery Server (Eureka)

### Configuration
```
infrastructure-services/discovery-server/pom.xml    - Eureka server dependencies
infrastructure-services/discovery-server/src/main/resources/
└── application.yaml                 - CONFIGURED: Port 8761
                                        Self-preservation disabled
                                        Auto-registration enabled
```

### Application Main Class
```
DiscoveryServerApplication.java      - UPDATED: Added @EnableEurekaServer annotation
```

### Features
- Service registration and discovery
- Health checking
- Load balancing support
- Eureka dashboard UI (http://localhost:8761)

---

## Config Server (Spring Cloud Config)

### Configuration
```
infrastructure-services/config-server/pom.xml       - Config server dependencies
infrastructure-services/config-server/src/main/resources/
└── application.yaml                 - CONFIGURED: Port 8888
                                        Git repository placeholder
                                        Default label: main
```

### Application Main Class
```
ConfigServerApplication.java         - UPDATED: Added @EnableConfigServer annotation
```

### Features
- Centralized configuration management
- Git-based configuration storage
- Property refresh support
- Profile-specific configurations

---

## Deployment Module

### Docker Configuration
```
deployment/
├── pom.xml                          - Deployment module POM
└── docker/
    ├── pom.xml                      - Docker build configuration
    ├── docker-compose.yml           - Multi-container orchestration (if created)
    └── src/
        ├── main/java/com/          - Docker-related utilities
        └── test/java/com/          - Docker container tests
```

### Kubernetes Configuration
```
deployment/kubernetes/
├── pom.xml                          - Kubernetes configuration POM
├── base/                            - Base Kubernetes manifests
│   ├── api-gateway.yaml             - Gateway deployment
│   ├── config-server.yaml           - Config server deployment
│   ├── discovery-server.yaml        - Eureka deployment
│   ├── inventory-service.yaml       - Inventory service deployment
│   ├── notification-service.yaml    - Notification service deployment
│   ├── order-service.yaml           - Order service deployment
│   └── product-service.yaml         - Product service deployment
├── overlays/                        - Environment-specific overlays
│   ├── dev/                         - Development environment
│   ├── prod/                        - Production environment
│   └── staging/                     - Staging environment
└── src/
    ├── main/resources/
    │   ├── archetype-resources/     - Template resources
    │   └── META-INF/                - Metadata
    └── test/java/com/               - Kubernetes tests
```

---

## Build Output

### JAR Files Generated
After `mvn clean install`, JAR files are located in:
```
{module}/target/{module}-0.0.1-SNAPSHOT.jar

Examples:
business-services/product-service/target/product-service-0.0.1-SNAPSHOT.jar
business-services/order-service/target/order-service-0.0.1-SNAPSHOT.jar
business-services/inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar
business-services/notification-service/target/notification-service-0.0.1-SNAPSHOT.jar
infrastructure-services/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
infrastructure-services/discovery-server/target/discovery-server-0.0.1-SNAPSHOT.jar
infrastructure-services/config-server/target/config-server-0.0.1-SNAPSHOT.jar
common-library/target/common-library-0.0.1-SNAPSHOT.jar
```

---

## Service Port Mapping

| Service | Port | Type | Database |
|---------|------|------|----------|
| Product Service | 8081 | REST | MongoDB |
| Inventory Service | 8082 | REST | MySQL |
| Order Service | 8083 | REST | MySQL |
| Notification Service | 8084 | REST | MySQL |
| API Gateway | 8080 | REST | N/A |
| Discovery Server | 8761 | HTTP | N/A |
| Config Server | 8888 | HTTP | N/A |

---

## Key Files Modified/Created Summary

### Created Files (6)
1. ✅ `common-library/src/main/java/com/binarylabyrinth/message/OrderPlacedEvent.java`
2. ✅ `common-library/src/main/java/com/binarylabyrinth/message/InventoryReservedEvent.java`
3. ✅ `common-library/src/main/java/com/binarylabyrinth/message/InventoryFailedEvent.java`
4. ✅ `common-library/src/main/java/com/binarylabyrinth/message/ProductCreatedEvent.java`
5. ✅ `business-services/notification-service/src/main/java/com/binarylabyrinth/notificationservice/entity/Notification.java`
6. ✅ `business-services/notification-service/src/main/java/com/binarylabyrinth/notificationservice/repository/NotificationRepository.java`

### Critical Files Modified (15+)
1. ✅ `business-services/order-service/src/main/java/com/binarylabyrinth/orderservice/dto/OrderRequestDto.java` - Fixed field name
2. ✅ `business-services/order-service/src/main/java/com/binarylabyrinth/orderservice/dto/OrderResponseDto.java` - Fixed field name
3. ✅ `business-services/order-service/src/main/java/com/binarylabyrinth/orderservice/mapper/OrderMapper.java` - Fixed mapping
4. ✅ `business-services/order-service/src/main/java/com/binarylabyrinth/orderservice/service/impl/OrderServiceImpl.java` - Updated imports
5. ✅ `business-services/inventory-service/src/main/java/com/binarylabyrinth/inventoryservice/controller/InventoryController.java` - Complete rewrite
6. ✅ `business-services/inventory-service/src/main/java/com/binarylabyrinth/inventoryservice/consumer/OrderPlacedConsumer.java` - Enhanced
7. ✅ `business-services/inventory-service/src/main/java/com/binarylabyrinth/inventoryservice/entity/Inventory.java` - Added imports
8. ✅ `business-services/inventory-service/src/main/java/com/binarylabyrinth/inventoryservice/event/InventoryReservedEvent.java` - Enhanced
9. ✅ `business-services/inventory-service/src/main/java/com/binarylabyrinth/inventoryservice/event/InventoryFailedEvent.java` - Enhanced
10. ✅ `business-services/product-service/src/main/java/com/binarylabyrinth/productservice/service/impl/ProductServiceImpl.java` - Updated imports
11. ✅ `business-services/notification-service/src/main/java/com/binarylabyrinth/notificationservice/consumer/OrderPlacedConsumer.java` - Updated imports
12. ✅ `business-services/notification-service/src/main/java/com/binarylabyrinth/notificationservice/service/impl/NotificationServiceImpl.java` - Enhanced implementation
13. ✅ `infrastructure-services/discovery-server/src/main/java/com/binarylabyrinth/discoveryserver/DiscoveryServerApplication.java` - Added annotation
14. ✅ `infrastructure-services/config-server/src/main/java/com/binarylabyrinth/configserver/ConfigServerApplication.java` - Added annotation
15. ✅ Multiple YAML configuration files - Complete configuration
16. ✅ Multiple POM files - Dependency updates

---

## Quick Reference

### To Run the Entire System
```bash
# Terminal 1: Discovery Server
cd infrastructure-services/discovery-server && mvn spring-boot:run

# Terminal 2: Config Server
cd infrastructure-services/config-server && mvn spring-boot:run

# Terminal 3-8: Services (in any order)
# business-services/product-service
# business-services/inventory-service
# business-services/order-service
# business-services/notification-service
# infrastructure-services/api-gateway
```

### Main Entry Point
```
http://localhost:8080 (API Gateway)
```

### Service Discovery Dashboard
```
http://localhost:8761 (Eureka)
```

### Documentation Files
- COMPLETION_SUMMARY.md - What was done
- TESTING_DEPLOYMENT_GUIDE.md - How to test and deploy
- PROJECT_STRUCTURE.md - File organization (this file)

---


