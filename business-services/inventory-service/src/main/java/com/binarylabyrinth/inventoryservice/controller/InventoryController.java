package com.binarylabyrinth.inventoryservice.controller;

import com.binarylabyrinth.inventoryservice.dto.InventoryRequestDto;
import com.binarylabyrinth.inventoryservice.dto.InventoryResponseDto;
import com.binarylabyrinth.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * InventoryController - REST API Endpoints for Inventory Management
 *
 * Provides REST endpoints for inventory operations:
 * - Check Stock: GET /api/inventory?productId=XX&quantity=YY
 *   Called by: Order Service (via Feign client)
 *   Returns: { inStock: true/false }
 *
 * - Add Inventory: POST /api/inventory
 *   Called by: Stock management staff
 *   Adds/updates stock for a product
 *
 * Endpoint Base Path: /api/inventory
 * Service: Inventory Service (Port 8082)
 * Via API Gateway: http://localhost:8080/api/inventory/**
 *
 * NOTE: This controller was FIXED - was previously a Kafka consumer (@Component)
 * Now properly implements REST endpoints for external service calls.
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    /** InventoryService implementation - injected by Spring */
    private final InventoryService inventoryService;

    /**
     * GET /api/inventory - Check if product is in stock
     *
     * Query parameters:
     * - productId: Product ID to check (required)
     * - quantity: Quantity needed (required)
     *
     * Called by:
     * 1. Order Service (via Feign client) during order placement
     * 2. External clients checking stock
     *
     * Process:
     * 1. Query MySQL for product inventory
     * 2. Check if quantity is available
     * 3. If available: Reserve quantity from stock
     * 4. Return response indicating stock availability
     *
     * @param productId Product ID to check (query parameter)
     * @param quantity Quantity needed (query parameter)
     * @return ResponseEntity with stock availability (HTTP 200)
     *         { inStock: true/false }
     */
    @GetMapping
    public ResponseEntity<InventoryResponseDto> isInStock(
            @RequestParam String productId,
            @RequestParam Integer quantity) {

        // READ-ONLY availability check (no longer mutates stock)
        return ResponseEntity.ok(
                inventoryService.isInStock(productId, quantity));
    }

    /**
     * POST /api/inventory/reserve - reserve (decrement) stock for an order.
     * Called by order-service during order placement. Returns
     * { inStock: true } and reduces stock when sufficient; { inStock: false }
     * with no change otherwise.
     */
    @PostMapping("/reserve")
    public ResponseEntity<InventoryResponseDto> reserve(
            @RequestParam String productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(
                inventoryService.reserveStock(productId, quantity));
    }

    /**
     * POST /api/inventory - Add or update inventory
     *
     * Request body:
     * {
     *   "productId": "product-123",
     *   "quantity": 100
     * }
     *
     * Called by:
     * 1. Stock management staff
     * 2. Warehouse systems
     * 3. Inventory synchronization services
     *
     * Process:
     * 1. Validate input (productId and quantity required)
     * 2. Create or update inventory record in MySQL
     * 3. Return 201 Created status
     *
     * @param requestDto Inventory data (validated)
     * @return Empty ResponseEntity (HTTP 201 Created)
     */
    @PostMapping
    public ResponseEntity<Void> addInventory(
            @Valid @RequestBody InventoryRequestDto requestDto) {

        // Call service to add/update inventory
        inventoryService.addInventory(requestDto);

        // Return 201 Created status
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * GET /api/inventory/all - list all inventory rows (admin dashboard).
     * Distinct from GET /api/inventory which requires productId+quantity
     * for the stock-check use case.
     */
    @GetMapping("/all")
    public ResponseEntity<java.util.List<com.binarylabyrinth.inventoryservice.entity.Inventory>> listAll() {
        return ResponseEntity.ok(inventoryService.listAll());
    }
}
