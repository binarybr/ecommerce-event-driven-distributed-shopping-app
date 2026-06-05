package com.binarylabyrinth.recommendationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Recommendation Service - co-purchase recommendation engine.
 *
 * Builds a denormalized user-product interaction view by consuming
 * Kafka events from other services, then serves recommendation queries
 * via SQL self-joins. No expensive ML — just intersection counting.
 *
 * Port: 8089
 * Database: MySQL (recommendation_service)
 * Kafka consumers: order-placed, review-submitted
 */
@SpringBootApplication
@EnableDiscoveryClient
public class RecommendationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendationServiceApplication.class, args);
    }
}
