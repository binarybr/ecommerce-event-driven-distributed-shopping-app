package com.binarylabyrinth.adminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Admin Service - aggregator microservice for the admin dashboard.
 *
 * Holds NO local state. Every endpoint composes data via Feign calls to
 * downstream services (user, product, order, inventory, notification, review,
 * recommendation). All endpoints require ROLE_ADMIN.
 *
 * Port: 8090
 * Discovery: Eureka client
 * Auth: JWT Bearer (validated + role-checked here)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class AdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
