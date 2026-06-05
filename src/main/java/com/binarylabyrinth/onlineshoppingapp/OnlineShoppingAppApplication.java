package com.binarylabyrinth.onlineshoppingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OnlineShoppingAppApplication - Root Application Entry Point
 *
 * This is the main Spring Boot application class for the Online Shopping Microservices project.
 * This is a root-level application that serves as the project's main entry point.
 *
 * NOTE: This application typically does NOT run in production. Instead, individual microservices
 * run independently:
 *
 * Individual Microservices (run separately on different ports):
 * - Product Service (Port 8081) - manages product catalog
 * - Inventory Service (Port 8082) - manages stock levels
 * - Order Service (Port 8083) - manages customer orders
 * - Notification Service (Port 8084) - sends email/SMS/push notifications
 *
 * Infrastructure Services (run separately):
 * - Discovery Server (Port 8761) - Eureka service registry
 * - Config Server (Port 8888) - centralized configuration
 * - API Gateway (Port 8080) - single entry point for all external requests
 *
 * To start the application:
 * 1. Using Docker Compose: docker-compose -f deployment/docker/docker-compose.yml up
 * 2. Individual services: mvn spring-boot:run (in each service directory)
 *
 * Architecture Overview:
 * - Microservices: Independently deployable services communicating via REST and Kafka
 * - API Gateway: Central entry point routing to backend services
 * - Service Discovery: Eureka registry for dynamic service location
 * - Message Broker: Kafka for asynchronous event-based communication
 * - Databases: MongoDB for Product Service, MySQL for others
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@SpringBootApplication
public class OnlineShoppingAppApplication {

    /**
     * Main method - Entry point for Spring Boot application
     *
     * @param args Command line arguments (optional)
     */
    public static void main(String[] args) {
        SpringApplication.run(OnlineShoppingAppApplication.class, args);
    }

}
