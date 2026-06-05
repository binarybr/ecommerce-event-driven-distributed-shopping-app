package com.binarylabyrinth.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserDto - Minimal user data for Order Service
 *
 * This DTO represents the minimal user information needed by Order Service
 * for order operations. It's used when retrieving user details from User Service
 * via Feign client to get customer email for notifications.
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;
}

