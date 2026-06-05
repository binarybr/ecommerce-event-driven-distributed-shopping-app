package com.binarylabyrinth.notificationservice.repository;

import com.binarylabyrinth.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * NotificationRepository - MySQL Data Access Object
 *
 * Extends JpaRepository to provide CRUD operations for Notification entity.
 * JpaRepository automatically provides:
 * - save(Notification): Create or update
 * - findById(Long): Retrieve by primary key
 * - findAll(): Retrieve all notifications
 * - delete(Notification): Delete record
 * - count(): Count total notifications
 * - exists(Long): Check existence
 *
 * Custom Queries:
 * - findByStatus(String): Find notifications by status (PENDING, SENT, FAILED)
 * - findByCreatedAtAfter(LocalDateTime): Find notifications created after date
 * - findByRecipient(String): Find all notifications for a recipient
 *
 * Generic types:
 * - Notification: Entity class
 * - Long: ID type (database primary key)
 *
 * Database: MySQL
 * Table: "notifications"
 * Connection: jdbc:mysql://localhost:3306/notification_service
 *
 * Usage:
 * - NotificationServiceImpl uses this to persist and retrieve notifications
 * - Enables audit trail and notification resend functionality
 * - Tracks delivery status for monitoring
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by status
     * Used to query notifications with specific status
     *
     * @param status Status value (PENDING, SENT, FAILED)
     * @return List of notifications matching the status
     */
    List<Notification> findByStatus(String status);

    /**
     * Find notifications created after specific date
     * Used for reporting and audit trails
     * Useful for finding notifications sent in a time range
     *
     * @param date Cutoff date (LocalDateTime)
     * @return List of notifications created after date
     */
    List<Notification> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find notifications by recipient
     * Used to get notification history for a specific recipient
     * Useful for customer support and resend operations
     *
     * @param recipient Recipient email, phone, or device ID
     * @return List of all notifications sent to this recipient
     */
    List<Notification> findByRecipient(String recipient);
}

