package com.binarylabyrinth.reviewservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Review Service - Spring Boot microservice for product reviews and ratings.
 *
 * Port: 8088
 * Database: MySQL (review_service)
 * Discovery: Eureka
 * Auth: JWT Bearer (forwarded from API Gateway)
 * Kafka: publishes 'review-submitted' on new reviews
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ReviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}
