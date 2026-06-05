package com.binarylabyrinth.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * InventoryServiceApplication - Main entry point for Inventory Service
 *
 * This is a Spring Boot microservice that manages product stock/inventory.
 *
 * Service Details:
 * - Port: 8082
 * - Database: MySQL
 * - Message Broker: Kafka
 * - Registry: Eureka (Discovery Server)
 * - Service Name: inventory-service
 *
 * Features:
 * - Stock availability checking via REST API
 * - Inventory management endpoints
 * - Kafka event consumption (order-placed events)
 * - Event publishing (inventory-reserved, inventory-failed)
 * - Global exception handling
 *
 * Integrations:
 * - Order Service: Calls this to check/reserve stock
 * - Kafka: Listens for order events and publishes inventory events
 * - Discovery Server: Registers itself for service discovery
 *
 * Startup Sequence:
 * 1. Spring Boot initializes all components
 * 2. Connects to MySQL database
 * 3. Registers with Eureka discovery server
 * 4. Connects to Kafka message broker
 * 5. Initializes Kafka consumers for event listening
 * 6. Starts embedded Tomcat server on port 8082
 *
 * API Base Path: http://localhost:8082/api/inventory
 * Via Gateway: http://localhost:8080/api/inventory
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@SpringBootApplication
public class InventoryServiceApplication {

    /**
     * Main method - Entry point for Spring Boot application
     * Initializes and starts the Inventory Service
     *
     * @param args Command line arguments (optional)
     */
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

}
