package com.binarylabyrinth.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * DiscoveryServerApplication - Eureka Service Discovery Server
 *
 * This is the central service registry for the microservices architecture.
 * All microservices register themselves with Eureka for dynamic discovery.
 *
 * RESPONSIBILITIES:
 * 1. Service Registration: Microservices register their availability
 * 2. Service Discovery: Other services discover available instances
 * 3. Health Checking: Monitors if registered services are alive
 * 4. Load Balancing: Enables client-side load balancing
 * 5. Failover: Tracks multiple instances of same service
 *
 * ARCHITECTURE:
 * ┌─────────────────────────────┐
 * │  Eureka Discovery Server    │
 * │  (Port 8761)                │
 * │  http://localhost:8761      │
 * └─────────────────────────────┘
 *              ↑
 *         Registers with
 *              ↓
 * ┌─────────────────────────────────────────────────────┐
 * │          Microservices                              │
 * ├─────────────────────────────────────────────────────┤
 * │ Product Service (8081)  ---- registered ----→       │
 * │ Order Service (8083)    ---- registered ----→       │
 * │ Inventory Service (8082) ---- registered ---→       │
 * │ Notification Service (8084) --- registered -→       │
 * │ API Gateway (8080)  ---- registered --------→       │
 * └─────────────────────────────────────────────────────┘
 *
 * CONFIGURATION (application.yaml):
 * - server.port: 8761
 * - eureka.instance.hostname: localhost
 * - eureka.server.enable-self-preservation: false (dev only)
 * - eureka.server.eviction-interval-timer-in-ms: 3000ms
 *
 * FEATURES:
 * 1. Dashboard UI at http://localhost:8761
 * 2. Shows all registered services
 * 3. Shows UP/DOWN status
 * 4. Shows instance counts
 * 5. REST API for queries
 *
 * SERVICE REGISTRATION:
 * Each service includes in pom.xml:
 * - spring-cloud-starter-netflix-eureka-client
 *
 * Each service includes in application.yaml:
 * - eureka.client.service-url.defaultZone: http://localhost:8761/eureka
 *
 * @EnableEurekaServer - Activates Eureka server functionality
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    /**
     * Main entry point for Discovery Server application
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }

}
