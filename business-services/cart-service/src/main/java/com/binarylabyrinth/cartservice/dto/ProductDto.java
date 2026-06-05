package com.binarylabyrinth.cartservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ProductDto - Minimal product data for Cart Service
 *
 * This DTO represents the minimal product information needed by Cart Service
 * for cart operations. It's used when retrieving product details from Product Service
 * via Feign client to get current pricing.
 *
 * Only includes necessary fields for cart operations (price, id, name).
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("price")
    private Double price;
}

