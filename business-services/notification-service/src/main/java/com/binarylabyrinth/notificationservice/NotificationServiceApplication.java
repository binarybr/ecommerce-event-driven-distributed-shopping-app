package com.binarylabyrinth.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NotificationServiceApplication - Main entry point for Notification Service
 *
 * This is a Spring Boot microservice that handles all notification delivery
 * for the e-commerce platform (email, SMS, push notifications).
 *
 * Service Details:
 * - Port: 8084
 * - Database: MySQL
 * - Message Broker: Kafka
 * - Registry: Eureka (Discovery Server)
 * - Service Name: notification-service
 *
 * Features:
 * - Send email notifications
 * - Send SMS notifications (infrastructure ready)
 * - Send push notifications (infrastructure ready)
 * - Track notification delivery status
 * - Kafka event consumption (order-placed, inventory events)
 * - Global exception handling
 *
 * Notification Triggers:
 * - Order Placed: Send confirmation email to customer
 * - Order Confirmed: Send inventory confirmation
 * - System Events: Configuration-based alerts
 *
 * Startup Sequence:
 * 1. Spring Boot initializes all components
 * 2. Connects to MySQL database
 * 3. Registers with Eureka discovery server
 * 4. Connects to Kafka message broker
 * 5. Initializes Kafka consumers for event listening
 * 6. Starts embedded Tomcat server on port 8084
 *
 * API Base Path: http://localhost:8084/api/notifications
 * Via Gateway: http://localhost:8080/api/notifications
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@SpringBootApplication
public class NotificationServiceApplication {

    /**
     * Main method - Entry point for Spring Boot application
     * Initializes and starts the Notification Service
     *
     * @param args Command line arguments (optional)
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}
