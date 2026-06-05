package com.binarylabyrinth.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Notification - JPA Entity (MySQL)
 *
 * Represents a notification record in the online shopping application.
 * This entity is persisted to MySQL in the 'notifications' table.
 *
 * Notifications can be of multiple types:
 * - EMAIL: Sent via SMTP
 * - SMS: Logged (SMS provider integration left for future)
 * - PUSH: Logged (Push notification provider integration left for future)
 *
 * Status tracking:
 * - PENDING: Notification created, waiting to be sent
 * - SENT: Successfully sent to recipient
 * - FAILED: Failed to send (error_message contains reason)
 *
 * Audit trail:
 * - createdAt: When notification was created
 * - sentAt: When notification was successfully sent
 * - errorMessage: Error details if sending failed
 *
 * Used by: Notification Service
 * Database: MySQL (localhost:3306/notification_service)
 * Table: notifications
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Notification {

    /** Auto-generated primary key - unique notification identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Recipient email/phone/device ID depending on type */
    private String recipient;

    /** Notification subject (for email) */
    private String subject;

    /** Notification message body (stored as TEXT for longer messages) */
    @Column(columnDefinition = "TEXT")
    private String message;

    /** Notification type: EMAIL, SMS, PUSH */
    private String type;

    /** Notification status: PENDING, SENT, FAILED */
    private String status;

    /** Timestamp of when notification was sent (null if not sent yet) */
    private LocalDateTime sentAt;

    /** Timestamp of when notification was created */
    private LocalDateTime createdAt;

    /** Error message if sending failed (null if sent successfully) */
    private String errorMessage;
}
