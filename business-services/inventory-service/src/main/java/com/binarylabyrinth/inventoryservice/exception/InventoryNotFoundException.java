package com.binarylabyrinth.inventoryservice.exception;

/**
 * InventoryNotFoundException - Custom exception for missing inventory records
 *
 * This exception is thrown when a product's inventory record cannot be found in the
 * database. This typically indicates that no stock record exists for a requested product.
 *
 * When this exception occurs:
 * 1. Inventory Service catches it during lookup
 * 2. GlobalExceptionHandler converts it to HTTP 404 Not Found response
 * 3. Error details are returned to client with descriptive message
 * 4. Order processing fails (inventory cannot be checked)
 *
 * Common Scenarios:
 * 1. Order Service calls inventory check for non-existent product
 * 2. Stock management API tries to update inventory for non-existent product
 * 3. Product deleted from Product Service but inventory record still referenced
 *
 * Example Usage:
 * ```
 * Inventory inventory = inventoryRepository.findByProductId(productId)
 *     .orElseThrow(() -> new InventoryNotFoundException(
 *         "No inventory found for product ID: " + productId));
 * ```
 *
 * HTTP Response:
 * ```
 * Status: 404 Not Found
 * Body: {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "No inventory found for product ID: XYZ123",
 *   "path": "/api/inventory?productId=XYZ123&quantity=1"
 * }
 * ```
 *
 * @author Binary Labyrinth
 * @version 1.0
 * @see com.binarylabyrinth.inventoryservice.handler.GlobalExceptionHandler
 * @see com.binarylabyrinth.inventoryservice.repository.InventoryRepository
 */
public class InventoryNotFoundException
        extends RuntimeException {

    /**
     * Constructs InventoryNotFoundException with error message
     *
     * @param message Descriptive error message explaining which inventory was not found
     *                Example: "No inventory found for product ID: PROD-12345"
     */
    public InventoryNotFoundException(
            String message){

        super(message);
    }
}