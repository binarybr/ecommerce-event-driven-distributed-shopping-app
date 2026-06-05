package com.binarylabyrinth.notificationservice.exception;

/**
 * NotificationException - Custom exception for notification failures
 *
 * This exception is thrown when notification delivery fails for any reason:
 * - SMTP connection issues (email sending)
 * - SMS provider API errors (message delivery)
 * - Push notification service unavailable
 * - Invalid recipient address/phone number
 * - Network timeouts or connectivity issues
 *
 * When this exception occurs:
 * 1. Notification Service catches it during send operation
 * 2. GlobalExceptionHandler converts it to HTTP 500 or appropriate status code
 * 3. Error details are logged for debugging
 * 4. Notification record is marked as FAILED in database
 * 5. Error reason is stored for retry logic
 *
 * Recovery Strategies:
 * - Retry with exponential backoff (handled by KafkaRetryConfig)
 * - Dead letter queue for permanently failed notifications
 * - Admin dashboard to monitor and requeue failed notifications
 * - Alternative notification channel fallback
 *
 * Example Usage:
 * ```
 * try {
 *     mailSender.send(message);
 * } catch (MailException e) {
 *     throw new NotificationException("Failed to send email: " + e.getMessage());
 * }
 * ```
 *
 * HTTP Response:
 * ```
 * Status: 500 Internal Server Error
 * Body: {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 500,
 *   "error": "Internal Server Error",
 *   "message": "Failed to send email: Connection timeout",
 *   "path": "/api/notifications/send"
 * }
 * ```
 *
 * @author Binary Labyrinth
 * @version 1.0
 * @see com.binarylabyrinth.notificationservice.handler.GlobalExceptionHandler
 * @see com.binarylabyrinth.notificationservice.service.impl.NotificationServiceImpl
 */
public class NotificationException
        extends RuntimeException {

    /**
     * Constructs NotificationException with error message
     *
     * @param message Descriptive error message explaining what failed
     *                Examples:
     *                - "Failed to send email: SMTP connection refused"
     *                - "Invalid recipient email address"
     *                - "SMS gateway timeout after 30 seconds"
     */
    public NotificationException(
            String message){

        super(message);
    }
}