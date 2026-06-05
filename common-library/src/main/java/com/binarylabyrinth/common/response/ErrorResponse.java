package com.binarylabyrinth.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ErrorResponse - Unified error response object
 *
 * This class is used across all microservices to provide consistent
 * error responses to API clients. It follows REST best practices for error handling.
 *
 * Usage: Global Exception Handlers in each service use this to format errors
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /** Timestamp when the error occurred */
    private LocalDateTime timestamp;

    /** HTTP status code (e.g., 404, 500, 400) */
    private int status;

    /** Error type/category (e.g., "Not Found", "Internal Server Error") */
    private String error;

    /** Detailed error message explaining what went wrong */
    private String message;

    /** The API endpoint path that was being called when the error occurred */
    private String path;
}
