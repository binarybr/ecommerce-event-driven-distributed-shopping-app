package com.binarylabyrinth.inventoryservice.service.impl;

import com.binarylabyrinth.inventoryservice.dto.InventoryRequestDto;
import com.binarylabyrinth.inventoryservice.dto.InventoryResponseDto;
import com.binarylabyrinth.inventoryservice.entity.Inventory;
import com.binarylabyrinth.inventoryservice.exception.InventoryNotFoundException;
import com.binarylabyrinth.inventoryservice.mapper.InventoryMapper;
import com.binarylabyrinth.inventoryservice.repository.InventoryRepository;
import com.binarylabyrinth.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * InventoryServiceImpl - Business Logic for Inventory Management
 *
 * Core responsibilities:
 * 1. Add/update product inventory (stock levels)
 * 2. Check stock availability for orders
 * 3. Reserve inventory when orders are placed
 * 4. Return inventory status
 * 5. Publish events to Kafka for other services
 *
 * KEY OPERATIONS:
 *
 * 1. addInventory():
 *    - Receives inventory request with productId and quantity
 *    - Creates or updates inventory record in MySQL
 *    - Called by: Stock management staff or warehouse systems
 *    - Response: HTTP 201 Created
 *
 * 2. isInStock():
 *    - Checks if product has sufficient stock
 *    - Called by: Order Service (via Feign client) when order is placed
 *    - If stock available: Reserves quantity (reduces inventory)
 *    - If stock NOT available: Returns false (order will fail)
 *    - CRITICAL: This operation reserves stock atomically
 *
 * FLOW DIAGRAM:
 * Order Service calls isInStock(productId, quantity)
 *              ↓
 * Query MySQL for product inventory
 *              ↓
 * Check if quantity >= requested quantity
 *         ↙               ↘
 *    YES (in stock)    NO (out of stock)
 *     ↓                   ↓
 * Reserve quantity   Return false
 * Update DB          No DB update
 * Return true
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl
        implements InventoryService {

    /** MySQL repository for inventory persistence */
    private final InventoryRepository inventoryRepository;

    /** Mapper for DTO ↔ Entity conversions */
    private final InventoryMapper inventoryMapper;

    /**
     * Add or update product inventory
     *
     * Process flow:
     * 1. Convert DTO to Entity using mapper
     * 2. Save to MySQL (creates new or updates existing)
     * 3. Log the operation for audit trail
     *
     * Called by: InventoryController.addInventory() via POST /api/inventory
     *
     * @param requestDto Inventory data (productId and quantity)
     *
     * Business Logic:
     * - If product already exists: Updates quantity
     * - If product is new: Creates new record
     *
     * Example:
     * POST /api/inventory
     * {
     *   "productId": "507f1f77bcf86cd799439011",
     *   "quantity": 100
     * }
     */
    @Override
    public void addInventory(
            InventoryRequestDto requestDto){

        // If inventory record exists for productId, increment quantity
        inventoryRepository.findByProductId(requestDto.getProductId())
                .ifPresentOrElse(existing -> {
                    existing.setQuantity(existing.getQuantity() + requestDto.getQuantity());
                    inventoryRepository.save(existing);
                }, () -> {
                    // Convert DTO to Entity and create new record
                    Inventory inventory = inventoryMapper.toEntity(requestDto);
                    inventoryRepository.save(inventory);
                });

        // Log for monitoring and audit trail
        log.info(
                "Inventory added for product : {}",
                requestDto.getProductId());
    }

    /**
     * Check if product is in stock and reserve if available
     *
     * CRITICAL OPERATION: This reserves stock immediately
     *
     * Process flow:
     * 1. Query MySQL for product inventory
     * 2. If product not found: Throw InventoryNotFoundException
     * 3. Compare available quantity with requested quantity
     * 4. If sufficient quantity:
     *    - Reduce quantity in database (reserve for order)
     *    - Return response with inStock = true
     * 5. If insufficient quantity:
     *    - Do NOT modify database
     *    - Return response with inStock = false
     * 6. Log operation for audit trail
     *
     * Called by: Order Service (via InventoryClient Feign client)
     * Called from: OrderServiceImpl.placeOrder()
     *
     * @param productId Product ID to check stock for
     * @param quantity Quantity required for order
     * @return InventoryResponseDto with inStock boolean flag
     * @throws InventoryNotFoundException if product not found
     *
     * Example:
     * GET /api/inventory?productId=507f1f77bcf86cd799439011&quantity=5
     * Response: { "inStock": true }
     *
     * Side Effect (if inStock=true):
     * - Inventory reduced in MySQL
     * - Stock is now "reserved" for this order
     * - Future orders will see reduced availability
     */
    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDto isInStock(
            String productId,
            Integer quantity){

        // READ-ONLY check — does not modify stock.
        boolean available = inventoryRepository.findByProductId(productId)
                .map(inv -> inv.getQuantity() >= quantity)
                .orElse(false);

        return InventoryResponseDto.builder()
                .inStock(available)
                .build();
    }

    @Override
    @Transactional
    public InventoryResponseDto reserveStock(
            String productId,
            Integer quantity){

        Inventory inventory =
                inventoryRepository.findByProductId(productId)
                        .orElseThrow(() ->
                                new InventoryNotFoundException("Inventory not found"));

        boolean available = inventory.getQuantity() >= quantity;

        if (available) {
            inventory.setQuantity(inventory.getQuantity() - quantity);
            inventoryRepository.save(inventory);  // @Version guards concurrent reserves
            log.info("Reserved {} units of product {} (remaining: {})",
                    quantity, productId, inventory.getQuantity());
        } else {
            log.warn("Insufficient stock to reserve {} units of product {} (available: {})",
                    quantity, productId, inventory.getQuantity());
        }

        return InventoryResponseDto.builder()
                .inStock(available)
                .build();
    }

    @Override
    @Transactional
    public void releaseStock(String productId, Integer quantity) {
        inventoryRepository.findByProductId(productId).ifPresentOrElse(
                inv -> {
                    inv.setQuantity(inv.getQuantity() + quantity);
                    inventoryRepository.save(inv);
                    log.info("Released {} units back to product {} (now: {})",
                            quantity, productId, inv.getQuantity());
                },
                () -> log.warn("Cannot release stock — no inventory row for product {}", productId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Inventory> listAll() {
        return inventoryRepository.findAll();
    }
}