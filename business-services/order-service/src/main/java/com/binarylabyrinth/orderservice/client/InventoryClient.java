package com.binarylabyrinth.orderservice.client;

import com.binarylabyrinth.orderservice.dto.InventoryResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * InventoryClient - Feign HTTP Client
 *
 * Provides declarative HTTP client for inter-service communication.
 * Automatically generates HTTP client code at compile time.
 *
 * Service Discovery:
 * - Configured via Eureka service discovery
 * - Service Name: "inventory-service"
 * - Automatically resolves to: lb://inventory-service
 * - Supports load balancing and failover
 *
 * Usage in OrderServiceImpl:
 * 1. Call inventoryClient.isInStock(productId, quantity)
 * 2. Wrapped with @CircuitBreaker for resilience
 * 3. Has fallback method for graceful degradation
 *
 * Benefits over RestTemplate:
 * - Declarative (no boilerplate HTTP code)
 * - Built-in load balancing
 * - Service discovery integration
 * - Easier to test with mocks
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    /**
     * Reserve (decrement) stock for an order.
     *
     * Makes HTTP POST to Inventory Service:
     * POST /api/inventory/reserve?productId=XX&quantity=YY
     *
     * Returns inStock=true if the reservation succeeded (stock decremented),
     * false if there wasn't enough. We use POST (not GET) because this mutates
     * state — the previous GET-with-side-effect was a REST anti-pattern.
     *
     * @param productId Product to reserve stock for
     * @param quantity Quantity needed
     * @return InventoryResponseDto with reservation result
     */
    @PostMapping("/api/inventory/reserve")
    InventoryResponseDto isInStock(
            @RequestParam String productId,
            @RequestParam Integer quantity);
}