package com.binarylabyrinth.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * KafkaConfig - Kafka Message Broker Configuration for Order Service
 *
 * Configures Kafka topics required by the Order Service.
 * This ensures topics are created automatically when the service starts.
 *
 * Topics Configured:
 * 1. order-placed: Published when new orders are placed
 *    - Partitions: 3 (for parallel processing across services)
 *    - Replication Factor: 1 (single node setup)
 *
 * Kafka Setup:
 * - Broker: localhost:9092
 * - Zookeeper: localhost:2181
 *
 * Event Publishing Flow:
 * 1. Order Service receives placeOrder request
 * 2. Creates order in MySQL
 * 3. Publishes OrderPlacedEvent to "order-placed" topic
 * 4. Multiple services subscribe and process:
 *    - Inventory Service: Reserves stock
 *    - Notification Service: Sends email confirmation
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Configuration
public class KafkaConfig {

    /**
     * Kafka Topic Bean - order-placed
     *
     * Creates a new Kafka topic for publishing order placement events.
     * Multiple subscribers can consume these events asynchronously.
     *
     * Topic Configuration:
     * - Name: "order-placed"
     * - Partitions: 3 (enables parallel message processing)
     * - Replication Factor: 1 (single broker, no redundancy)
     *
     * Subscribers:
     * - Inventory Service: Listens for orders to reserve stock
     * - Notification Service: Listens to send order confirmations
     *
     * @return NewTopic configured for order-placed events
     */
    @Bean
    public NewTopic orderPlacedTopic(){

        return new NewTopic(
                "order-placed",
                3,
                (short) 1);
    }
}