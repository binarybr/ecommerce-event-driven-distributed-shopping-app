package com.binarylabyrinth.notificationservice.handler;

import com.binarylabyrinth.common.response.ErrorResponse;
import com.binarylabyrinth.notificationservice.exception.NotificationException;
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
 * GlobalExceptionHandler - Centralized Exception Handling for Notification Service
 *
 * This class intercepts exceptions thrown by service methods/controllers
 * and converts them into appropriate HTTP responses with consistent format.
 *
 * EXCEPTION HANDLING STRATEGY:
 * 1. NotificationException: HTTP 400 Bad Request
 *    - Triggered when notification delivery fails
 *    - Reasons: SMTP timeout, invalid email, provider error, etc.
 *    - Status is persisted to database as FAILED
 *
 * 2. MethodArgumentNotValidException: HTTP 400 Bad Request
 *    - Triggered when request DTO validation fails
 *    - Returns map of field-level validation errors
 *    - Format: {"field": "error message", ...}
 *
 * ERROR RESPONSE FORMATS:
 *
 * Notification Exception:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 400,
 *   "error": "BAD_REQUEST",
 *   "message": "Failed to send email: Connection timeout",
 *   "path": "/api/notifications/email"
 * }
 *
 * Validation Exception:
 * {
 *   "recipient": "Email must be valid",
 *   "subject": "Subject is required",
 *   "message": "Message cannot be blank"
 * }
 *
 * NOTIFICATION SERVICE ERROR HANDLING:
 * - Email sending failures are logged and stored with FAILED status
 * - Notifications can be retried by admin or automated retry logic
 * - Original error message preserved for debugging
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
     * Handle NotificationException (HTTP 400)
     *
     * Called when sending notifications (email/SMS/push) fails.
     * Returns client error indicating the reason for failure.
     *
     * Common failure scenarios:
     * 1. Email sending: SMTP server unreachable, timeout, invalid address
     * 2. SMS sending: Provider API error, invalid phone number
     * 3. Push notification: Firebase/APNs connectivity issue
     *
     * Behavior:
     * - Exception is CAUGHT and logged
     * - Notification record is marked as FAILED
     * - Error message is stored for audit trail
     * - Client receives HTTP 400 with error details
     *
     * Retry Strategy:
     * - Admin can manually retry from dashboard
     * - Kafka retry config may attempt automatic retry if configured
     * - Dead letter queue available for permanent failures
     *
     * @param ex NotificationException with failure reason
     * @param request Current HTTP request (for context)
     * @return ResponseEntity with ErrorResponse (HTTP 400)
     */
    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ErrorResponse> handleNotificationException(
            NotificationException ex,
            HttpServletRequest request) {

        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .error(HttpStatus.BAD_REQUEST.name())
                        .message(ex.getMessage())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Handle MethodArgumentNotValidException (HTTP 400)
     *
     * Called when @Valid validation fails on request DTO fields.
     * Returns field-level validation error details.
     *
     * Common validation failures:
     * - Missing required fields (@NotBlank, @NotNull)
     * - Invalid field format (@Email, @Pattern)
     * - Value out of range (@Min, @Max)
     * - Invalid enum values
     *
     * Response Format: Map of field names to error messages
     * {
     *   "recipient": "Must be a valid email address",
     *   "subject": "Subject cannot be blank"
     * }
     *
     * Validation Annotations Used:
     * - @NotBlank: String cannot be null or empty
     * - @NotNull: Field cannot be null
     * - @Email: Must be valid email format
     * - @Min / @Max: Numeric range validation
     *
     * @param ex MethodArgumentNotValidException with field errors
     * @return ResponseEntity with validation error map (HTTP 400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex) {

        // Create map to store field validation errors
        Map<String, String> errors = new HashMap<>();

        // Extract all field-level validation errors
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(
                        error.getField(),
                        error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }
}
