package com.binarylabyrinth.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ProductServiceApplication - Main entry point for Product Service
 * 
 * This is a Spring Boot microservice that manages product catalog.
 * 
 * Service Details:
 * - Port: 8081
 * - Database: MongoDB
 * - Message Broker: Kafka
 * - Registry: Eureka (Discovery Server)
 * - Service Name: product-service
 * 
 * Features:
 * - Product CRUD operations via REST API
 * - Kafka event publishing (product-created events)
 * - Caching layer for performance
 * - Global exception handling
 * 
 * Startup Sequence:
 * 1. Spring Boot initializes all components
 * 2. Connects to MongoDB database
 * 3. Registers with Eureka discovery server
 * 4. Connects to Kafka message broker
 * 5. Starts embedded Tomcat server on port 8081
 * 
 * API Base Path: http://localhost:8081/api/products
 * Via Gateway: http://localhost:8080/api/products
 * 
 * @author Binary Labyrinth
 * @version 1.0
 */
@SpringBootApplication
public class ProductServiceApplication {

    /**
     * Main method - Entry point for Spring Boot application
     * Initializes and starts the Product Service
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

}
