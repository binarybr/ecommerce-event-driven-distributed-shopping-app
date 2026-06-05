package com.binarylabyrinth.orderservice.client;

import com.binarylabyrinth.orderservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ProductClient - Feign HTTP Client for Product Service.
 *
 * Used during order placement to fetch the AUTHORITATIVE unit price from the
 * product catalog. order-service recomputes the order total from this value
 * (unitPrice * quantity) instead of trusting the client-supplied price, which
 * closes a billing-integrity hole where a client could forge a low price.
 *
 * Service Discovery: Uses Eureka (lb://product-service).
 * The GET /api/products/{id} endpoint is public, so no auth header is required.
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    /**
     * Get product details (including current catalog price) by ID.
     *
     * @param id Product ID (MongoDB ObjectId as String)
     * @return ProductDto with at least the price populated
     * @throws feign.FeignException.NotFound if the product does not exist
     */
    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable String id);
}
