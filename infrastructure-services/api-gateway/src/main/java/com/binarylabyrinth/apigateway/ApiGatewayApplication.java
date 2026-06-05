package com.binarylabyrinth.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ApiGatewayApplication - Spring Cloud API Gateway
 *
 * This is the main entry point for all external API requests in the microservices architecture.
 * The gateway acts as a single entry point (facade) for all backend services.
 *
 * RESPONSIBILITIES:
 * 1. Request Routing: Route requests to appropriate backend services
 * 2. Load Balancing: Distribute requests across multiple instances (via Eureka)
 * 3. Request Filtering: Pre-processing and post-processing of requests
 * 4. Authentication/Authorization: Enforce security policies
 * 5. Rate Limiting: Prevent abuse and control traffic
 * 6. Monitoring: Track API usage and performance
 *
 * ARCHITECTURE:
 * ┌────────────────────────────────────────────────────────┐
 * │             External Clients                           │
 * │  (Web Browser, Mobile App, Third-party Services)      │
 * └────────────────────────────────────────────────────────┘
 *                         │
 *                         ↓ HTTP Requests
 * ┌────────────────────────────────────────────────────────┐
 * │        API Gateway (Port 8080)                         │
 * │  Routing, Security, Load Balancing                    │
 * │  http://localhost:8080                                │
 * └────────────────────────────────────────────────────────┘
 *      │         │         │         │
 *      ↓         ↓         ↓         ↓
 * ┌────────┐ ┌─────────┐ ┌────────────┐ ┌──────────────┐
 * │Product │ │  Order  │ │ Inventory  │ │ Notification │
 * │Service │ │ Service │ │  Service   │ │  Service     │
 * │:8081   │ │  :8083  │ │   :8082    │ │    :8084     │
 * └────────┘ └─────────┘ └────────────┘ └──────────────┘
 *
 * ROUTE DEFINITIONS (configured in application.yaml):
 * 1. /api/products/** → http://product-service:8081
 * 2. /api/orders/** → http://order-service:8083
 * 3. /api/inventory/** → http://inventory-service:8082
 * 4. /api/notifications/** → http://notification-service:8084
 *
 * FEATURES:
 * 1. Service Discovery: Uses Eureka (lb://service-name)
 * 2. Path Stripping: Remove /api prefix before forwarding
 * 3. Load Balancing: Distributes across multiple instances
 * 4. Resilience: Handles service failures gracefully
 * 5. Monitoring: Tracks request metrics
 *
 * BENEFITS:
 * - Single URL for clients: http://localhost:8080
 * - Decouples clients from backend services
 * - Enables service migration without client changes
 * - Centralized security and policy enforcement
 * - Single authentication/authorization point
 * - Service versioning support
 * - Rate limiting and throttling
 *
 * CONFIGURATION (application.yaml):
 * - server.port: 8080
 * - spring.cloud.gateway.routes: Route definitions
 * - spring.cloud.gateway.default-filters: Common filters
 *
 * FUTURE ENHANCEMENTS:
 * - JWT token validation
 * - Request throttling/rate limiting
 * - Circuit breaker pattern
 * - Request/response transformation
 * - CORS configuration
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@SpringBootApplication
public class ApiGatewayApplication {

    /**
     * Main entry point for API Gateway application
     *
     * Gateway startup flow:
     * 1. Starts on port 8080
     * 2. Registers with Eureka (service discovery)
     * 3. Loads route configuration from application.yaml
     * 4. Reads backend service endpoints from application.yaml
     * 5. Starts routing incoming requests to backend services
     * 6. Monitors health of backend services
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
