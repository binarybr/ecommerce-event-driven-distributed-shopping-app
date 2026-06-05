package com.binarylabyrinth.cartservice.client;

import com.binarylabyrinth.cartservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ProductClient - Feign HTTP Client for Product Service
 *
 * Provides inter-service communication with Product Service to fetch product details
 * including prices for cart calculations.
 *
 * Service Discovery: Uses Eureka (lb://product-service)
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    /**
     * Get product details by ID
     *
     * @param id Product ID (MongoDB ObjectId as String)
     * @return ProductDto with product details including price
     * @throws org.springframework.web.client.HttpClientErrorException if product not found
     */
    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable String id);
}

