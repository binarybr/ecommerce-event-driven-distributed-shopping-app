package com.binarylabyrinth.productservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * KafkaConfig - Kafka Message Broker Configuration
 *
 * Configures Kafka topics required by the Product Service.
 * This ensures topics are created automatically when the service starts.
 *
 * Topics Configured:
 * 1. product-created: Published when new products are created
 *    - Partitions: 3 (for parallel processing)
 *    - Replication Factor: 1 (single node setup)
 *
 * Kafka Setup:
 * - Broker: localhost:9092
 * - Zookeeper: localhost:2181
 *
 * Event Publishing Flow:
 * 1. Product Service creates new product
 * 2. ProductCreatedEvent published to "product-created" topic
 * 3. Other services subscribe and process the event
 *    (e.g., Notification Service sends notifications)
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Configuration
public class KafkaConfig {

    /**
     * Kafka Topic Bean - product-created
     *
     * Creates a new Kafka topic for publishing product creation events.
     *
     * Topic Configuration:
     * - Name: "product-created"
     * - Partitions: 3 (enables parallel message processing)
     * - Replication Factor: 1 (single broker, no redundancy)
     *
     * @return NewTopic configured for product-created events
     */
    @Bean
    public NewTopic productTopic(){

        return new NewTopic(
                "product-created",
                3,
                (short) 1);
    }
}