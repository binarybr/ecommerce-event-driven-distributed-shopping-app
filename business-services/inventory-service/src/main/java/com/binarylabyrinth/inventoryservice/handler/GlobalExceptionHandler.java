package com.binarylabyrinth.inventoryservice.handler;

import com.binarylabyrinth.common.response.ErrorResponse;
import com.binarylabyrinth.inventoryservice.exception.InventoryNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * GlobalExceptionHandler - Centralized Exception Handling for Inventory Service
 *
 * This class intercepts exceptions thrown by service methods/controllers
 * and converts them into appropriate HTTP responses with consistent format.
 *
 * EXCEPTION HANDLING STRATEGY:
 * 1. InventoryNotFoundException: HTTP 404 Not Found
 *    - Triggered when product inventory record doesn't exist
 *    - Indicates no stock tracking for requested product
 *    - Common when: Product is new or hasn't been added to inventory yet
 *
 * ERROR RESPONSE FORMAT (Consistent across all services):
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 404,
 *   "error": "NOT_FOUND",
 *   "message": "No inventory found for product ID: PROD-123",
 *   "path": "/api/inventory"
 * }
 *
 * INVENTORY SERVICE EXCEPTION HANDLING:
 * - When Order Service calls isInStock(): Returns InventoryResponseDto
 * - If product not found: Throws InventoryNotFoundException → HTTP 404
 * - Caller (Order Service) should handle 404 or will fail order placement
 *
 * @RestControllerAdvice: Enables this class to handle exceptions globally
 * All endpoints in this service will use these exception handlers
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle InventoryNotFoundException (HTTP 404)
     *
     * Called when trying to check/manage inventory for a product
     * that has no stock record in the database.
     *
     * Common scenarios:
     * 1. Order Service calls isInStock() for new product without inventory
     * 2. Stock management tries to update non-existent product
     * 3. Product exists but never initialized in inventory system
     *
     * Solution: Admin should first add inventory via POST /api/inventory
     *
     * Response:
     * - Status: 404 Not Found
     * - Message: Details which productId was not found
     * - Path: URI that triggered the error
     *
     * @param ex InventoryNotFoundException with product ID
     * @param request Current HTTP request (for context)
     * @return ResponseEntity with ErrorResponse (HTTP 404)
     */
    @ExceptionHandler(
            InventoryNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleInventoryNotFoundException(
            InventoryNotFoundException ex,
            HttpServletRequest request){

        return ResponseEntity.status(
                        HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.builder()
                                .error("NOT_FOUND")
                                .message(ex.getMessage())
                                .status(
                                        HttpStatus.NOT_FOUND.value())
                                .path(
                                        request.getRequestURI())
                                .timestamp(
                                        LocalDateTime.now())
                                .build()
                );
    }
}