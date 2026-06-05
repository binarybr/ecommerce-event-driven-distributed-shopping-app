package com.binarylabyrinth.inventoryservice.service;

import com.binarylabyrinth.inventoryservice.dto.InventoryRequestDto;
import com.binarylabyrinth.inventoryservice.dto.InventoryResponseDto;

/**
 * InventoryService - Business Logic Interface
 *
 * Defines core service operations for inventory management.
 * This interface is implemented by InventoryServiceImpl which handles:
 * - Stock availability checking
 * - Inventory reservation (during order placement)
 * - Inventory updates
 * - Kafka event publishing
 * - Database persistence
 * - Error handling
 *
 * Implementation: InventoryServiceImpl
 *
 * Key Responsibilities:
 * 1. Check if sufficient stock is available for an order
 * 2. Reserve stock when order is placed (reduce available quantity)
 * 3. Add/update inventory when new stock arrives
 * 4. Publish events (inventory-reserved or inventory-failed)
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
public interface InventoryService {

    /**
     * Add or update inventory for a product
     * - Creates new inventory record if doesn't exist
     * - Updates quantity if product already exists
     * - Used for stock replenishment
     */
    void addInventory(
            InventoryRequestDto requestDto);

    /**
     * Check if product is in stock and reserve if available
     * - Queries MySQL for product inventory
     * - Verifies quantity is available
     * - If available: Reserves quantity (reduces available stock)
     * - Publishes inventory-reserved or inventory-failed event
     * - Returns response indicating stock status
     */
    /**
     * READ-ONLY availability check. Does NOT modify stock.
     * (Previously this method also decremented — that side-effect moved to
     * reserveStock to keep GET semantics safe/idempotent.)
     */
    InventoryResponseDto isInStock(
            String productId,
            Integer quantity);

    /**
     * Reserve (decrement) stock if available. Returns inStock=true and reduces
     * the quantity when sufficient; returns inStock=false and leaves stock
     * untouched otherwise. Called by order-service during order placement.
     */
    InventoryResponseDto reserveStock(
            String productId,
            Integer quantity);

    /**
     * Release (restock) a previously reserved quantity — used when an order is
     * cancelled or deleted. No-op if the product has no inventory row.
     */
    void releaseStock(String productId, Integer quantity);

    /** List all inventory rows (admin dashboard). */
    java.util.List<com.binarylabyrinth.inventoryservice.entity.Inventory> listAll();
}