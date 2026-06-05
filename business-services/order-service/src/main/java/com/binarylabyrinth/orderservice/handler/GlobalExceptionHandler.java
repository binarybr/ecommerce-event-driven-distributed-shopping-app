package com.binarylabyrinth.orderservice.handler;

import com.binarylabyrinth.common.response.ErrorResponse;
import com.binarylabyrinth.orderservice.exception.OrderNotFoundException;
import com.binarylabyrinth.orderservice.exception.ProductOutOfStockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * GlobalExceptionHandler - Centralized Exception Handling for Order Service
 *
 * This class intercepts exceptions thrown by service methods/controllers
 * and converts them into appropriate HTTP responses with consistent format.
 *
 * EXCEPTION HANDLING STRATEGY:
 * 1. ProductOutOfStockException: HTTP 400 Bad Request
 *    - Triggered when customer orders a product with insufficient inventory
 *    - Client error (can retry after inventory is updated)
 *
 * 2. OrderNotFoundException: HTTP 404 Not Found
 *    - Triggered when trying to retrieve/delete non-existent order
 *    - Resource not found error
 *
 * ERROR RESPONSE FORMAT (Consistent across all services):
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 404,
 *   "error": "NOT_FOUND",
 *   "message": "Order not found with id: 123",
 *   "path": "/api/orders/123"
 * }
 *
 * @RestControllerAdvice: Enables this class to handle exceptions globally
 * @ExceptionHandler: Marks method to handle specific exception types
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle OrderNotFoundException (HTTP 404)
     *
     * Called when trying to retrieve/delete an order that doesn't exist.
     * Returns standardized error response with 404 status.
     *
     * Example scenarios:
     * - GET /api/orders/999 (non-existent order ID)
     * - DELETE /api/orders/999 (non-existent order ID)
     *
     * @param ex OrderNotFoundException with order ID
     * @param request Current HTTP request (for context)
     * @return ResponseEntity with ErrorResponse (HTTP 404)
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleOrderNotFoundException(
            OrderNotFoundException ex,
            HttpServletRequest request){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.builder()
                                .error("NOT_FOUND")
                                .message(ex.getMessage())
                                .status(HttpStatus.NOT_FOUND.value())
                                .path(request.getRequestURI())
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    /**
     * Handle ProductOutOfStockException (HTTP 400)
     *
     * Called when customer tries to order a product with insufficient inventory.
     * Returns client error (bad request) indicating product is out of stock.
     *
     * Example scenarios:
     * - POST /api/orders (requesting 10 units but only 5 available)
     * - Product inventory has been exhausted
     *
     * Suggests: Customer should retry later after more stock arrives
     *
     * @param ex ProductOutOfStockException with reason
     * @param request Current HTTP request (for context)
     * @return ResponseEntity with ErrorResponse (HTTP 400)
     */
    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<ErrorResponse>
    handleProductOutOfStockException(
            ProductOutOfStockException ex,
            HttpServletRequest request){

        return ResponseEntity.badRequest()
                .body(
                        ErrorResponse.builder()
                                .error("BAD_REQUEST")
                                .message(ex.getMessage())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .path(request.getRequestURI())
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }
}
