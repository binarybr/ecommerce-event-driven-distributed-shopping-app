package com.binarylabyrinth.productservice.handler;

import com.binarylabyrinth.productservice.exception.ErrorResponse;
import com.binarylabyrinth.productservice.exception.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - Centralized Exception Handling
 *
 * This class intercepts exceptions thrown by service methods/controllers
 * and converts them into appropriate HTTP responses.
 *
 * Features:
 * - Consistent error response format across all endpoints
 * - Proper HTTP status codes (404, 400, 500)
 * - Informative error messages
 * - Request context preservation (URI, timestamp)
 *
 * Exception Handlers:
 * 1. ProductNotFoundException: 404 NOT_FOUND
 * 2. MethodArgumentNotValidException: 400 BAD_REQUEST (validation errors)
 * 3. General Exception: 500 INTERNAL_SERVER_ERROR (catch-all)
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ProductNotFoundException
     *
     * Called when a product ID is not found in the database.
     * Returns 404 Not Found with error details.
     *
     * @param ex ProductNotFoundException with error message
     * @param request Current HTTP request (for URL context)
     * @return ResponseEntity with ErrorResponse (HTTP 404)
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex, HttpServletRequest request) {

        // Build error response with timestamp, status, error type, message, and path
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.name())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle MethodArgumentNotValidException
     *
     * Called when @Valid validation fails on request DTO fields.
     * Returns 400 Bad Request with field-level error messages.
     *
     * @param ex MethodArgumentNotValidException with validation errors
     * @return ResponseEntity with field validation error map (HTTP 400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {

        // Extract field validation errors
        Map<String, String> errors = new HashMap<>();

        // Populate error map with field names and their error messages
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handle any unhandled exceptions
     *
     * Catch-all handler for exceptions not specifically handled.
     * Returns 500 Internal Server Error.
     *
     * @param ex Exception that occurred
     * @param request Current HTTP request (for URL context)
     * @return ResponseEntity with ErrorResponse (HTTP 500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {

        // Build generic error response
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}