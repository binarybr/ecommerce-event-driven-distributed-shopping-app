package com.binarylabyrinth.inventoryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * InventoryRequestDto - API Request Data Transfer Object
 *
 * This DTO represents the data structure expected in API POST requests
 * for adding or updating product inventory.
 *
 * Validation Annotations:
 * - @NotBlank: Field cannot be null or empty (for strings)
 * - @NotNull: Field cannot be null (for numeric types)
 *
 * Used by: InventoryController.addInventory()
 *
 * Example JSON Request Body:
 * {
 *   "productId": "507f1f77bcf86cd799439011",
 *   "quantity": 100
 * }
 *
 * This endpoint is called by:
 * 1. Stock management staff/systems
 * 2. Warehouse inventory synchronization
 * 3. Automated stock replenishment systems
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequestDto {

    /**
     * Product ID - cannot be blank
     * Format: MongoDB ObjectId as String
     * Validation: Required field
     */
    @NotBlank
    private String productId;

    /**
     * Quantity in stock - cannot be null
     * Validation: Required field
     * Format: Positive integer representing units available
     */
    @NotNull
    private Integer quantity;
}