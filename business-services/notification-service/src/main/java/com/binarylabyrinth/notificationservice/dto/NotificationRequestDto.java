package com.binarylabyrinth.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NotificationRequestDto - API Request Data Transfer Object
 *
 * This DTO represents the data structure expected in API POST requests
 * for sending notifications (email, SMS, push).
 *
 * Validation Annotations:
 * - @NotBlank: Field cannot be null or empty
 *
 * Used by: All NotificationController POST methods
 * - sendEmail()
 * - sendSms()
 * - sendPush()
 *
 * Example JSON Request Body (Email):
 * {
 *   "recipient": "customer@example.com",
 *   "subject": "Order Confirmation",
 *   "message": "Your order #123 has been placed successfully"
 * }
 *
 * Example JSON Request Body (SMS):
 * {
 *   "recipient": "+1234567890",
 *   "subject": "N/A",
 *   "message": "Your order has been placed"
 * }
 *
 * Note: For SMS/Push, subject field can be empty or repurposed
 * All fields follow same validation for simplicity
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {

    /**
     * Recipient contact information - cannot be blank
     * Format depends on notification type:
     * - EMAIL: email@example.com
     * - SMS: +1234567890 (phone number)
     * - PUSH: device-token or user-id
     * Validation: Required field
     */
    @NotBlank(message = "Recipient is required")
    private String recipient;

    /**
     * Notification subject - cannot be blank
     * Primarily used for email subject line
     * For SMS/Push: Can contain a title or be set to empty
     * Validation: Required field
     */
    @NotBlank(message = "Subject is required")
    private String subject;

    /**
     * Notification message body - cannot be blank
     * Validation: Required field
     * Format: Plain text (can be enhanced for HTML emails)
     * Length: Can be longer for emails, shorter for SMS
     */
    @NotBlank(message = "Message is required")
    private String message;
}
