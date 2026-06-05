package com.binarylabyrinth.inventoryservice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * InventoryResponseDto - API Response Data Transfer Object
 *
 * This is a simple response object returned by the inventory check endpoint.
 * It only contains a boolean flag indicating whether stock is available.
 *
 * Used by: InventoryController.isInStock()
 * Called by: Order Service (via Feign client)
 *
 * Example JSON Response:
 * {
 *   "inStock": true
 * }
 *
 * Response scenarios:
 * 1. inStock = true:
 *    - Sufficient quantity available
 *    - Stock has been reserved for the order
 *    - Order Service can proceed with order creation
 *
 * 2. inStock = false:
 *    - Insufficient quantity or product not found
 *    - Stock has NOT been reserved
 *    - Order Service should reject/fail the order
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Data
@Builder
public class InventoryResponseDto {

    /**
     * Stock availability flag
     * - true: Sufficient stock available and reserved
     * - false: Insufficient stock or product not found
     */
    private boolean inStock;
}