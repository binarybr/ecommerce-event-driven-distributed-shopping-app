package com.binarylabyrinth.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * OrderServiceApplication - Main entry point for Order Service
 *
 * This is a Spring Boot microservice that manages order placement and tracking.
 *
 * Service Details:
 * - Port: 8083
 * - Database: MySQL
 * - Message Broker: Kafka
 * - Registry: Eureka (Discovery Server)
 * - Service Name: order-service
 *
 * Features:
 * - Order placement and management via REST API
 * - Kafka event publishing (order-placed events)
 * - Feign client integration with Inventory Service
 * - Global exception handling
 *
 * Feign Clients:
 * - InventoryClient: Communicates with Inventory Service for stock validation
 *   Uses service discovery to locate Inventory Service (localhost:8082)
 *
 * Startup Sequence:
 * 1. Spring Boot initializes all components
 * 2. Connects to MySQL database
 * 3. Registers with Eureka discovery server
 * 4. Connects to Kafka message broker
 * 5. Initializes Feign clients for inter-service communication
 * 6. Starts embedded Tomcat server on port 8083
 *
 * API Base Path: http://localhost:8083/api/orders
 * Via Gateway: http://localhost:8080/api/orders
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {

    /**
     * Main method - Entry point for Spring Boot application
     * Initializes and starts the Order Service
     *
     * @param args Command line arguments (optional)
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
