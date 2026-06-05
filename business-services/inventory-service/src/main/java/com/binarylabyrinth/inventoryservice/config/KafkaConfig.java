package com.binarylabyrinth.inventoryservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * KafkaConfig - pre-creates the topics inventory-service publishes to, so
 * downstream consumers (order-service) can subscribe immediately without
 * racing first-publish. 3 partitions / replication-factor 1 (single-broker dev).
 *
 *   - inventory-reserved : emitted after stock is successfully reserved
 *   - inventory-failed   : emitted when reservation can't be fulfilled
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic inventoryReservedTopic(){
        return new NewTopic("inventory-reserved", 3, (short) 1);
    }

    @Bean
    public NewTopic inventoryFailedTopic(){
        return new NewTopic("inventory-failed", 3, (short) 1);
    }
}