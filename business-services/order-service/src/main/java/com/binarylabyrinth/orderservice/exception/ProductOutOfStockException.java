package com.binarylabyrinth.orderservice.exception;

/**
 * ProductOutOfStockException - Custom exception for out-of-stock scenarios
 *
 * This exception is thrown when a customer attempts to order a product that has
 * insufficient inventory.
 *
 * When this exception occurs:
 * 1. Order Service catches it during order processing
 * 2. GlobalExceptionHandler converts it to HTTP 400 Bad Request response
 * 3. Error details are returned to client with descriptive message
 * 4. Order is NOT persisted to database
 * 5. Inventory Service is NOT called
 *
 * Example Usage:
 * ```
 * if (requestedQuantity > availableStock) {
 *     throw new ProductOutOfStockException(
 *         "Product " + productId + " only has " + availableStock + " units available");
 * }
 * ```
 *
 * HTTP Response:
 * ```
 * Status: 400 Bad Request
 * Body: {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Product ID only has X units available",
 *   "path": "/api/orders"
 * }
 * ```
 *
 * @author Binary Labyrinth
 * @version 1.0
 * @see com.binarylabyrinth.orderservice.handler.GlobalExceptionHandler
 */
public class ProductOutOfStockException
        extends RuntimeException {

    /**
     * Constructs ProductOutOfStockException with error message
     *
     * @param message Descriptive error message explaining the stock shortage
     *                Example: "Product XYZ has only 2 units, but 5 requested"
     */
    public ProductOutOfStockException(String message) {
        super(message);
    }
}