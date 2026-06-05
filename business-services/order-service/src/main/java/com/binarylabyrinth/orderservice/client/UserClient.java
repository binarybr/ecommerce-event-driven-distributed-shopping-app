package com.binarylabyrinth.orderservice.client;

import com.binarylabyrinth.orderservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * UserClient - Feign HTTP Client for User Service
 *
 * Provides inter-service communication with User Service to fetch user details
 * including email addresses for order notifications.
 *
 * Service Discovery: Uses Eureka (lb://user-service)
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@FeignClient(name = "user-service")
public interface UserClient {

    /**
     * Get user details by ID
     *
     * @param id User ID
     * @return UserDto with user details including email
     * @throws org.springframework.web.client.HttpClientErrorException if user not found
     */
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable String id);
}

