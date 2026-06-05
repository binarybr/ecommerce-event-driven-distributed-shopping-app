package com.binarylabyrinth.orderservice.exception;

/**
 * OrderNotFoundException - Custom exception for missing order records
 *
 * This exception is thrown when a customer or admin tries to retrieve or update
 * an order that doesn't exist in the MySQL database.
 *
 * When this exception occurs:
 * 1. Order Service catches it during order lookup
 * 2. GlobalExceptionHandler converts it to HTTP 404 Not Found response
 * 3. Error details are returned to client with descriptive message
 *
 * Common Scenarios:
 * 1. Client requests order status for non-existent order ID
 * 2. Admin tries to cancel an order that was already deleted
 * 3. Order ID is invalid or from a different system
 *
 * Example Usage:
 * ```
 * Order order = orderRepository.findById(orderId)
 *     .orElseThrow(() -> new OrderNotFoundException(orderId));
 * ```
 *
 * HTTP Response:
 * ```
 * Status: 404 Not Found
 * Body: {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Order not found with id : 999",
 *   "path": "/api/orders/999"
 * }
 * ```
 *
 * @author Binary Labyrinth
 * @version 1.0
 * @see com.binarylabyrinth.orderservice.handler.GlobalExceptionHandler
 * @see com.binarylabyrinth.orderservice.repository.OrderRepository
 */
public class OrderNotFoundException
        extends RuntimeException {

    /**
     * Constructs OrderNotFoundException with order ID
     *
     * @param id The order ID that was not found in the database
     */
    public OrderNotFoundException(Long id){

        super("Order not found with id : " + id);
    }
}