package com.binarylabyrinth.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * KafkaConsumerConfig - Kafka Consumer Configuration
 *
 * This configuration enables and configures Kafka consumers for the Notification Service.
 *
 * PURPOSE:
 * - Enable Kafka consumer listener detection
 * - Provide beans for Kafka consumer functionality
 * - Configure consumer group and connection settings
 *
 * WHAT IT DOES:
 * 1. @EnableKafka annotation enables automatic detection of @KafkaListener methods
 * 2. Spring scans all components for @KafkaListener annotated methods
 * 3. Automatically creates Kafka consumers for each listener found
 * 4. Connects to Kafka broker specified in application.yaml
 * 5. Registers consumer with group "notification-group"
 *
 * KAFKA LISTENERS ENABLED:
 * - OrderPlacedConsumer.consume() - Listens to "order-placed" topic
 *   Receives orders and sends confirmation notifications to customers
 *
 * CONFIGURATION PROPERTIES:
 * The actual Kafka connection properties are defined in application.yaml:
 * ```
 * spring.kafka.bootstrap-servers: localhost:9092
 * spring.kafka.consumer.group-id: notification-group
 * spring.kafka.consumer.auto-offset-reset: earliest
 * spring.kafka.consumer.value-deserializer: ...JsonDeserializer
 * ```
 *
 * ERROR HANDLING:
 * - If Kafka broker is unavailable at startup, the service still starts
 * - Connection attempts are retried with exponential backoff
 * - Message deserialization errors are logged
 * - Failed messages can be sent to dead letter topic (if configured)
 *
 * @author Binary Labyrinth
 * @version 1.0
 * @see com.binarylabyrinth.notificationservice.consumer.OrderPlacedConsumer
 * @see org.springframework.kafka.annotation.KafkaListener
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    /**
     * Kafka consumer configuration is now automatic through Spring Boot.
     * This class primarily serves as a configuration marker to enable Kafka listeners.
     *
     * In Spring Boot 3.x with spring-kafka, most consumer configuration is
     * externalized to application.yaml and handled automatically.
     *
     * Custom beans or configurations can be added here if needed for:
     * - Custom error handlers
     * - Custom deserializers
     * - Consumer interceptors
     * - Listener container customization
     */
}